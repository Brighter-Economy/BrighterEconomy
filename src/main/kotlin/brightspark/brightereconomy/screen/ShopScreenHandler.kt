package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlockEntity
import io.wispforest.owo.client.screens.OwoScreenHandler
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ShopScreenHandler(
	syncId: Int,
	playerInventory: PlayerInventory,
	shopBlockEntity: ShopBlockEntity? = null
) : ScreenHandler(BrighterEconomy.SHOP_SCREEN_HANDLER, syncId) {
	var cost: Int by property(shopBlockEntity?.cost ?: 0)

	init {
		playerInventory.onOpen(playerInventory.player)

		// Shop slot
		addSlot(Slot(shopBlockEntity ?: SimpleInventory(1), 0, 29, 21))

		// Player inventory slots
		val invStartX = 8
		val invStartY = 71
		for (y in 0..2) {
			for (x in 0..8) {
				addSlot(Slot(playerInventory, x + y * 9 + 9, invStartX + x * 18, invStartY + y * 18))
			}
		}
		for (x in 0..8) {
			addSlot(Slot(playerInventory, x, invStartX + x * 18, invStartY + 18 * 3 + 4))
		}
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

	override fun quickMove(player: PlayerEntity, slotIndex: Int): ItemStack {
		if (slotIndex != 0) {
			val shopSlot = slots[0]
			shopSlot.stack = slots[slotIndex].stack
			shopSlot.markDirty()
		}
		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity): Boolean = true

	inner class OwoPropertyDelegate<T>(private val owoProperty: SyncedProperty<T>) :
		ReadWriteProperty<OwoScreenHandler, T> {
		override fun getValue(thisRef: OwoScreenHandler, property: KProperty<*>): T = owoProperty.get()
		override fun setValue(thisRef: OwoScreenHandler, property: KProperty<*>, value: T) = owoProperty.set(value)
	}

	private inline fun <reified T> property(initialValue: T): OwoPropertyDelegate<T> =
		OwoPropertyDelegate(createProperty(T::class.java, initialValue))
}
