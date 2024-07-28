package brightspark.brightereconomy.screen

import brightspark.brightereconomy.blocks.ShopBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Util
import java.util.*

@Suppress("LeakingThis")
abstract class ShopScreenHandler(
	type: ScreenHandlerType<*>,
	syncId: Int,
	protected val playerInventory: PlayerInventory,
	shopBlockEntity: ShopBlockEntity?,
	playerInvX: Int,
	playerInvY: Int
) : ScreenHandler(type, syncId) {
	protected val ownerUuid: UUID = shopBlockEntity?.owner ?: Util.NIL_UUID

	init {
		playerInventory.onOpen(playerInventory.player)

		for (y in 0..2) {
			for (x in 0..8) {
				addSlot(Slot(playerInventory, x + y * 9 + 9, playerInvX + x * 18, playerInvY + y * 18))
			}
		}
		for (x in 0..8) {
			addSlot(Slot(playerInventory, x, playerInvX + x * 18, playerInvY + 18 * 3 + 4))
		}
	}

	override fun canUse(player: PlayerEntity?): Boolean = true

	override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack = ItemStack.EMPTY
}
