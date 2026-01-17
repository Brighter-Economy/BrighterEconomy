package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.economy.PlayerAccountListener
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.network.screen.CustomerScreenPurchasePacket
import brightspark.brightereconomy.util.Util
import brightspark.brightereconomy.util.getSpaceFor
import brightspark.brightereconomy.util.property
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class ShopCustomerScreenHandler(
	syncId: Int,
	playerInventory: PlayerInventory,
	private val shopBlockEntity: ShopBlockEntity? = null
) : ShopScreenHandler(BrighterEconomy.SHOP_CUSTOMER_SCREEN_HANDLER, syncId, playerInventory, shopBlockEntity, 8, 79),
	PlayerAccountListener {

	var playerAccount: SyncedProperty<PlayerAccount> =
		property(EconomyService.getAccount(playerInventory.player.uuid))
		private set
	var forSaleStack: SyncedProperty<ItemStack> =
		property(shopBlockEntity, ShopBlockEntity::getStack, ShopBlockEntity::setStack, ItemStack.EMPTY)
		private set
	var cost: SyncedProperty<Int> = property(shopBlockEntity, ShopBlockEntity::cost, ShopBlockEntity::setCost, 0)
		private set
	var stock: SyncedProperty<Int> = property(shopBlockEntity, ShopBlockEntity::getStockAmount, { _, _ -> }, 0)
		private set

	init {
		addServerboundMessage(CustomerScreenPurchasePacket::class.java, this::handlePurchasePacket)
		shopBlockEntity?.addListener(this)
	}

	override fun onClosed(player: PlayerEntity?) {
		super.onClosed(player)
		shopBlockEntity?.removeListener(this)
	}

	override fun handlePlayerAccountUpdate(account: PlayerAccount) {
		if (account.uuid == playerInventory.player.uuid)
			this.playerAccount.set(account)
	}

	fun playerInvSpace(forSaleStack: ItemStack): Int = playerInventory.getSpaceFor(forSaleStack)

	fun playerCanBuy(num: Int, cost: Int, account: PlayerAccount) = !account.locked && account.money >= (num * cost)

	fun sendPurchase(num: Int): Unit = sendMessage(CustomerScreenPurchasePacket(num))

	fun handlePurchasePacket(packet: CustomerScreenPurchasePacket) {
		BrighterEconomy.LOG.atInfo()
			.setMessage("Handling shop purchase at {}")
			.addArgument { shopBlockEntity?.pos?.toShortString() }
			.log()

		shopBlockEntity ?: run {
			BrighterEconomy.LOG.atError().setMessage("Can't handle shop purchase - no shop block entity").log()
			return
		}

		val player = playerInventory.player
		val playerUuid = playerAccount.get().uuid
		val itemAmount = forSaleStack.get().count * packet.amount
		val cost = cost.get().toLong() * packet.amount

		val simulatedExchangeResult = EconomyService.simulateExchange(playerUuid, ownerUuid, cost)
		if (simulatedExchangeResult != TransactionExchangeResult.SUCCESS) {
			player.sendMessage(failureMessageText(itemAmount, cost, simulatedExchangeResult.text))
		}

		val exchangeResult = EconomyService.purchase(
			shopBlockEntity.shopId,
			playerUuid,
			ownerUuid,
			cost,
			forSaleStack.get().copyWithCount(itemAmount),
			player.nameForScoreboard
		)
		when (exchangeResult) {
			TransactionExchangeResult.SUCCESS -> handlePurchaseSuccess(player, itemAmount, cost)
			// This shouldn't happen as should be caught in pre-purchase checks, but just in-case
			else -> player.sendMessage(failureMessageText(itemAmount, cost, exchangeResult.text))
		}
	}

	private fun handlePurchaseSuccess(player: PlayerEntity, itemAmount: Int, cost: Long) {
		// Remove from storage
		val stacksToGive = shopBlockEntity!!.removeStock(itemAmount)
		if (stacksToGive.isEmpty()) {
			player.sendMessage(
				failureMessageText(
					itemAmount,
					cost,
					Text.translatable("text.brightereconomy.player_shop.purchase.failure.no_items")
				)
			)
			return
		}

		// Notify player
		player.sendMessage(
			Util.messageTextSecondary(
				"text.brightereconomy.player_shop.purchase.success",
				itemAmount.toString(),
				forSaleStack.get().name,
				Util.formatMoney(cost),
				Util.getUsername(ownerUuid) ?: "<unknown>"
			).styled { it.withItalic(true) }
		)

		// Give to player
		stacksToGive.forEach {
			player.giveItemStack(it)
			if (!it.isEmpty)
				player.dropStack(it)
		}
	}

	private fun failureMessageText(itemAmount: Int, cost: Long, failureReason: Text): Text = Util.messageTextSecondary(
		"text.brightereconomy.player_shop.purchase.failure",
		itemAmount.toString(),
		forSaleStack.get().name,
		Util.formatMoney(cost),
		Util.getUsername(ownerUuid) ?: "<unknown>",
		failureReason
	).styled { it.withItalic(true) }
}
