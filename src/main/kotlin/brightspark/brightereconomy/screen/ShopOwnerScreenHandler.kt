package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.network.SetShopDataPacket
import brightspark.brightereconomy.util.property
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

class ShopOwnerScreenHandler(
	syncId: Int,
	playerInventory: PlayerInventory,
	shopBlockEntity: ShopBlockEntity? = null
) : ShopScreenHandler(BrighterEconomy.SHOP_OWNER_SCREEN_HANDLER, syncId, playerInventory, shopBlockEntity, 8, 71) {

	var cost: SyncedProperty<Int> = property(shopBlockEntity, ShopBlockEntity::cost, 0)

	init {
		addServerboundMessage(SetShopDataPacket::class.java) {
			if (player().uuid == ownerUuid) {
				getShopStack().count = it.itemCount
				cost.set(it.cost)
			}
		}

		addSlot(Slot(shopBlockEntity ?: SimpleInventory(1), 0, 39, 42))
	}

	fun getShopStack(): ItemStack = slots[0].stack

	override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
		if (slotIndex == 0 && (actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE)) {
			val slot = slots[slotIndex]
			val heldStack = cursorStack
			if (cursorStack.isEmpty)
				slot.stack = ItemStack.EMPTY
			else {
				when (actionType) {
					SlotActionType.PICKUP -> slot.stack = heldStack.copy()
					SlotActionType.QUICK_MOVE -> slot.stack = ItemStack.EMPTY
					else -> Unit
				}
			}
			slot.markDirty()
			return
		}

		super.onSlotClick(slotIndex, button, actionType, player)
	}

	override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
		if (slot != 0) {
			val shopSlot = slots[0]
			shopSlot.stack = slots[slot].stack
			shopSlot.markDirty()
		}
		return ItemStack.EMPTY
	}

	fun sendData(cost: Int): Unit = sendMessage(SetShopDataPacket(getShopStack().count, cost))
}
