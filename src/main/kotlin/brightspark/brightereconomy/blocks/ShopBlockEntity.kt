package brightspark.brightereconomy.blocks

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.screen.ShopScreenHandler
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SingleStackInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.util.*

class ShopBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BrighterEconomy.SHOP_BLOCK_ENTITY, pos, state),
	NamedScreenHandlerFactory, SingleStackInventory {

	var owner: UUID? = null
	var cost: Int = 0
	private var itemStackForSale: ItemStack = ItemStack.EMPTY
	var linkedContainer: BlockPos? = null

	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity?): ScreenHandler =
		ShopScreenHandler(syncId, playerInventory)

	override fun getDisplayName(): Text = Text.translatable(cachedState.block.translationKey)

	override fun getStack(slot: Int): ItemStack = if (slot == 0) itemStackForSale else ItemStack.EMPTY

	override fun removeStack(slot: Int, amount: Int): ItemStack =
		if (slot == 0) {
			itemStackForSale.decrement(amount)
			if (itemStackForSale.isEmpty)
				itemStackForSale = ItemStack.EMPTY
			itemStackForSale
		} else {
			ItemStack.EMPTY
		}

	override fun setStack(slot: Int, stack: ItemStack) {
		if (slot == 0)
			itemStackForSale = stack
	}

	override fun canPlayerUse(player: PlayerEntity): Boolean = Inventory.canPlayerUse(this, player)

	override fun readNbt(nbt: NbtCompound) {
		super.readNbt(nbt)
		owner = if (nbt.containsUuid("owner")) nbt.getUuid("owner") else null
		cost = nbt.getInt("cost")
		itemStackForSale = ItemStack.fromNbt(nbt.getCompound("stackForSale"))
		linkedContainer = if (nbt.contains("container")) BlockPos.fromLong(nbt.getLong("container")) else null
	}

	override fun writeNbt(nbt: NbtCompound) {
		super.writeNbt(nbt)
		owner?.let { nbt.putUuid("owner", it) }
		nbt.putInt("cost", cost)
		nbt.put("stackForSale", itemStackForSale.writeNbt(NbtCompound()))
		linkedContainer?.let { nbt.putLong("container", it.asLong()) }
	}
}
