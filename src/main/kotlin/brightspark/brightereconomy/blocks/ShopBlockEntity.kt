package brightspark.brightereconomy.blocks

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.screen.ShopCustomerScreenHandler
import brightspark.brightereconomy.screen.ShopOwnerScreenHandler
import brightspark.brightereconomy.shops.ShopTrackerService
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
import net.minecraft.world.World
import java.util.*

class ShopBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BrighterEconomy.SHOP_BLOCK_ENTITY, pos, state),
	NamedScreenHandlerFactory, SingleStackInventory {

	var shopId: UUID = UUID.randomUUID()
		private set
	var owner: UUID = Util.NIL_UUID
		private set
	var cost: Int = 0
		private set
	private var itemStackForSale: ItemStack = ItemStack.EMPTY

	var linkedContainer: BlockPos = BlockPos.ORIGIN
		private set
	private val linkedInventory: Inventory?
		get() = if (linkedContainer != BlockPos.ORIGIN) world!!.getBlockEntity(linkedContainer) as Inventory else null

	private val listeners: MutableSet<ShopCustomerScreenHandler> = mutableSetOf()
	private var cachedStock: Int = 0

	fun addListener(listener: ShopCustomerScreenHandler) {
		listeners += listener
	}

	fun removeListener(listener: ShopCustomerScreenHandler) {
		listeners -= listener
	}

	fun notifyListeners(function: (ShopCustomerScreenHandler) -> Unit) {
		listeners.forEach(function)
	}

	fun tick(world: World) {
		if ((world.time + 6).mod(20) != 0 || listeners.isEmpty()) return

		// Check if there's been any changes to the linked container's inventory
		linkedInventory?.let { inv ->
			val stock = (0 until inv.size()).asSequence()
				.map { inv.getStack(it) }
				.filter { ItemStack.areItemsEqual(it, itemStackForSale) }
				.sumOf { it.count }
			if (cachedStock != stock) {
				cachedStock = stock
				notifyListeners { it.stock.set(stock) }
			}
		}
	}

	fun setOwner(ownerUuid: UUID) {
		owner = ownerUuid
		markDirty()
	}

	fun getStockAmount(): Int = linkedInventory?.count(itemStackForSale.item) ?: 0

	fun removeStock(amount: Int): List<ItemStack> {
		val inv = linkedInventory ?: return emptyList()
		val stacks = mutableListOf<ItemStack>()
		var amountLeftNeeded = amount
		for (i in (0..<inv.size()).reversed()) {
			val stack = inv.getStack(i)
			if (ItemStack.areItemsEqual(stack, itemStackForSale)) {
				val stackCount = stack.count
				when {
					stackCount < amountLeftNeeded -> {
						stacks += stack
						amountLeftNeeded -= stackCount
					}
					stackCount == amountLeftNeeded -> {
						stacks += stack
						break
					}
					else -> {
						stacks += stack.split(amountLeftNeeded)
						break
					}
				}
			}
		}
		return stacks
	}

	fun setCost(cost: Int) {
		this.cost = cost
		ShopTrackerService.updateShop(shopId) { it.copy(price = cost) }
		markDirty()
	}

	fun setLinkedContainer(pos: BlockPos) {
		linkedContainer = pos
		markDirty()
	}

	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler =
		if (player.uuid == owner)
			ShopOwnerScreenHandler(syncId, playerInventory, this)
		else
			ShopCustomerScreenHandler(syncId, playerInventory, this)

	override fun getDisplayName(): Text = Text.translatable(cachedState.block.translationKey)

	override fun getStack(slot: Int): ItemStack = if (slot == 0) itemStackForSale else ItemStack.EMPTY

	override fun removeStack(slot: Int, amount: Int): ItemStack =
		if (slot == 0) {
			itemStackForSale.decrement(amount)
			if (itemStackForSale.isEmpty)
				itemStackForSale = ItemStack.EMPTY
			markDirty()
			itemStackForSale
		} else {
			ItemStack.EMPTY
		}

	override fun setStack(slot: Int, stack: ItemStack) {
		if (slot != 0) return
		itemStackForSale = stack
		ShopTrackerService.updateShop(shopId) { it.copy(itemStack = stack) }
		markDirty()
	}

	override fun canPlayerUse(player: PlayerEntity): Boolean = Inventory.canPlayerUse(this, player)

	override fun readNbt(nbt: NbtCompound) {
		super.readNbt(nbt)
		shopId = nbt.getUuid("shopId")
		owner = nbt.getUuid("owner")
		cost = nbt.getInt("cost")
		itemStackForSale = ItemStack.fromNbt(nbt.getCompound("stackForSale"))
		linkedContainer = BlockPos.fromLong(nbt.getLong("container"))
	}

	override fun writeNbt(nbt: NbtCompound) {
		super.writeNbt(nbt)
		nbt.putUuid("shopId", shopId)
		nbt.putUuid("owner", owner)
		nbt.putInt("cost", cost)
		nbt.put("stackForSale", itemStackForSale.writeNbt(NbtCompound()))
		nbt.putLong("container", linkedContainer.asLong())
	}
}
