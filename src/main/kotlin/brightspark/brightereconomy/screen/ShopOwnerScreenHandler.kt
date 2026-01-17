package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.network.screen.SetShopDataPacket
import brightspark.brightereconomy.util.property
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.ClickType

class ShopOwnerScreenHandler(
	syncId: Int,
	playerInventory: Inventory,
	shopBlockEntity: ShopBlockEntity? = null
) : ShopScreenHandler(BrighterEconomy.SHOP_OWNER_SCREEN_HANDLER, syncId, playerInventory, shopBlockEntity, 8, 71) {

	private val shopSlotId: Int
	var cost: SyncedProperty<Int> = property(shopBlockEntity, ShopBlockEntity::cost, ShopBlockEntity::setCost, 0)

	init {
		addServerboundMessage(SetShopDataPacket::class.java) {
			if (player().uuid == ownerUuid) {
				getShopStack().count = it.itemCount
				cost.set(it.cost)
			}
		}

		shopSlotId = addSlot(Slot(shopBlockEntity ?: SimpleContainer(1), 0, 39, 42)).index
	}

	fun getShopStack(): ItemStack = slots[shopSlotId].item

	override fun clicked(slotIndex: Int, button: Int, actionType: ClickType, player: Player) {
		if (slotIndex == shopSlotId && (actionType == ClickType.PICKUP || actionType == ClickType.QUICK_MOVE)) {
			val slot = slots[slotIndex]
			val heldStack = carried
			if (carried.isEmpty)
				slot.setByPlayer(ItemStack.EMPTY)
			else {
				when (actionType) {
					ClickType.PICKUP -> slot.setByPlayer(heldStack.copy())
					ClickType.QUICK_MOVE -> slot.setByPlayer(ItemStack.EMPTY)
				}
			}
			slot.setChanged()
			return
		}

		super.clicked(slotIndex, button, actionType, player)
	}

	override fun quickMoveStack(player: Player, slot: Int): ItemStack {
		if (slot != shopSlotId) {
			val shopSlot = slots[shopSlotId]
			shopSlot.setByPlayer(slots[slot].item)
			shopSlot.setChanged()
		}
		return ItemStack.EMPTY
	}

	fun sendData(cost: Int): Unit = sendMessage(SetShopDataPacket(getShopStack().count, cost))
}
