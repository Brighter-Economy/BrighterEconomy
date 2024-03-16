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
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import java.util.*

class ShopBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BrighterEconomy.SHOP_BLOCK_ENTITY, pos, state),
	NamedScreenHandlerFactory, SingleStackInventory {

	var owner: UUID = Util.NIL_UUID
	var cost: Int = 0
	private var itemStackForSale: ItemStack = ItemStack.EMPTY
	var linkedContainer: BlockPos = BlockPos.ORIGIN

	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity?): ScreenHandler =
		ShopScreenHandler(syncId, playerInventory, this)

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
		owner = nbt.getUuid("owner")
		cost = nbt.getInt("cost")
		itemStackForSale = ItemStack.fromNbt(nbt.getCompound("stackForSale"))
		linkedContainer = BlockPos.fromLong(nbt.getLong("container"))
	}

	override fun writeNbt(nbt: NbtCompound) {
		super.writeNbt(nbt)
		nbt.putUuid("owner", owner)
		nbt.putInt("cost", cost)
		nbt.put("stackForSale", itemStackForSale.writeNbt(NbtCompound()))
		nbt.putLong("container", linkedContainer.asLong())
	}
}
