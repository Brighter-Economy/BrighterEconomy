package brightspark.brightereconomy.screen

import brightspark.brightereconomy.blocks.ShopBlockEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.Util
import java.util.*

@Suppress("LeakingThis")
abstract class ShopScreenHandler(
	type: MenuType<*>,
	syncId: Int,
	protected val playerInventory: Inventory,
	shopBlockEntity: ShopBlockEntity?,
	playerInvX: Int,
	playerInvY: Int
) : AbstractContainerMenu(type, syncId) {
	protected val ownerUuid: UUID = shopBlockEntity?.owner ?: Util.NIL_UUID

	init {
		playerInventory.startOpen(playerInventory.player)

		for (y in 0..2) {
			for (x in 0..8) {
				addSlot(Slot(playerInventory, x + y * 9 + 9, playerInvX + x * 18, playerInvY + y * 18))
			}
		}
		for (x in 0..8) {
			addSlot(Slot(playerInventory, x, playerInvX + x * 18, playerInvY + 18 * 3 + 4))
		}
	}

	override fun stillValid(player: Player): Boolean = true

	override fun quickMoveStack(player: Player, slot: Int): ItemStack = ItemStack.EMPTY
}
