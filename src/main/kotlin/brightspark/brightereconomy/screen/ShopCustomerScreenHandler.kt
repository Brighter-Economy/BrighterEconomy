package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.economy.EconomyState
import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.network.CustomerScreenPurchasePacket
import brightspark.brightereconomy.util.Util
import brightspark.brightereconomy.util.getSpaceFor
import brightspark.brightereconomy.util.property
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.min

class ShopCustomerScreenHandler(
	syncId: Int,
	playerInventory: PlayerInventory,
	shopBlockEntity: ShopBlockEntity? = null
) : ShopScreenHandler(BrighterEconomy.SHOP_CUSTOMER_SCREEN_HANDLER, syncId, playerInventory, shopBlockEntity, 8, 79) {

	var playerAccount: SyncedProperty<PlayerAccount> =
		property(EconomyState.get().getAccount(playerInventory.player.uuid))
		private set
	var forSaleStack: SyncedProperty<ItemStack> =
		property(shopBlockEntity, ShopBlockEntity::getStack, ShopBlockEntity::setStack, ItemStack.EMPTY)
		private set
	var cost: SyncedProperty<Int> = property(shopBlockEntity, ShopBlockEntity::cost, 0)
		private set
	var stock: SyncedProperty<Int> = property(shopBlockEntity, ShopBlockEntity::getStockAmount, { _, _ -> }, 0)
		private set

	init {
		addServerboundMessage(CustomerScreenPurchasePacket::class.java) { packet ->
			BrighterEconomy.LOG.atInfo()
				.setMessage("Handling shop purchase at {}")
				.addArgument { shopBlockEntity?.pos?.toShortString() }
				.log()

			val player = playerInventory.player
			val itemAmount = forSaleStack.get().count * packet.amount
			val cost = cost.get().toLong() * packet.amount
			val result = EconomyState.get()
				.exchange(playerAccount.get().uuid, ownerUuid, cost, player.entityName)

			if (result == TransactionExchangeResult.SUCCESS) {
				fun whiteText(): MutableText = Text.empty().styled { it.withFormatting(Formatting.WHITE) }
				fun whiteText(text: String): Text = Text.literal(text).styled { it.withFormatting(Formatting.WHITE) }
				player.sendMessage(
					Text.translatable(
						"screen.brightereconomy.player_shop.purchase.success",
						whiteText(itemAmount.toString()),
						whiteText().append(forSaleStack.get().name),
						whiteText(Util.formatMoney(cost)),
						whiteText(Util.getUsername(ownerUuid) ?: "<unknown>")
					).styled { it.withFormatting(Formatting.GRAY, Formatting.ITALIC) }
				)

				var amountToGive = packet.amount
				val maxStackSize = forSaleStack.get().maxCount
				while (amountToGive > 0) {
					val count = min(maxStackSize, amountToGive)
					val stack = forSaleStack.get().copyWithCount(count)
					val success = player.giveItemStack(stack)
					if (success)
						amountToGive -= count
					else {
						amountToGive -= (count - stack.count)
						player.dropStack(stack)
					}
				}
			}
		}
	}

	fun playerInvSpace(forSaleStack: ItemStack): Int = playerInventory.getSpaceFor(forSaleStack)

	fun playerCanBuy(num: Int, cost: Int, account: PlayerAccount) = !account.locked && account.money >= (num * cost)

	fun sendPurchase(num: Int): Unit = sendMessage(CustomerScreenPurchasePacket(num))
}
