package brightspark.brightereconomy.blocks

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.screen.ShopCustomerScreenHandler
import brightspark.brightereconomy.screen.ShopOwnerScreenHandler
import brightspark.brightereconomy.shops.ShopTrackerService
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.Container
import net.minecraft.world.ticks.ContainerSingleItem
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup
import net.minecraft.world.MenuProvider
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.chat.Component
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import java.util.*

class ShopBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BrighterEconomy.SHOP_BLOCK_ENTITY, pos, state),
	MenuProvider, ContainerSingleItem {

	var shopId: UUID = UUID.randomUUID()
		private set
	var owner: UUID = Util.NIL_UUID
		private set
	var cost: Int = 0
		private set
	private var itemStackForSale: ItemStack = ItemStack.EMPTY

	var linkedContainer: BlockPos = BlockPos.ZERO
		private set
	private val linkedInventory: Container?
		get() = if (linkedContainer != BlockPos.ZERO) level!!.getBlockEntity(linkedContainer) as Container else null

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

	fun tick(world: Level) {
		if ((world.gameTime + 6).mod(20) != 0 || listeners.isEmpty()) return

		// Check if there's been any changes to the linked container's inventory
		linkedInventory?.let { inv ->
			val stock = (0 until inv.containerSize).asSequence()
				.map { inv.getItem(it) }
				.filter { ItemStack.isSameItem(it, itemStackForSale) }
				.sumOf { it.count }
			if (cachedStock != stock) {
				cachedStock = stock
				notifyListeners { it.stock.set(stock) }
			}
		}
	}

	fun setOwner(ownerUuid: UUID) {
		owner = ownerUuid
		setChanged()
	}

	fun getStockAmount(): Int = linkedInventory?.countItem(itemStackForSale.item) ?: 0

	fun removeStock(amount: Int): List<ItemStack> {
		val inv = linkedInventory ?: return emptyList()
		val stacks = mutableListOf<ItemStack>()
		var amountLeftNeeded = amount
		for (i in (0..<inv.containerSize).reversed()) {
			val stack = inv.getItem(i)
			if (ItemStack.isSameItem(stack, itemStackForSale)) {
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
		setChanged()
	}

	fun setLinkedContainer(pos: BlockPos) {
		linkedContainer = pos
		setChanged()
	}

	override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu =
		if (player.uuid == owner)
			ShopOwnerScreenHandler(syncId, playerInventory, this)
		else
			ShopCustomerScreenHandler(syncId, playerInventory, this)

	override fun getDisplayName(): Component = Component.translatable(blockState.block.descriptionId)

	override fun getTheItem(): ItemStack = itemStackForSale

	override fun setTheItem(stack: ItemStack) {
		itemStackForSale = stack
		ShopTrackerService.updateShop(shopId) { it.copy(itemStack = stack) }
		setChanged()
	}

	override fun removeItem(slot: Int, amount: Int): ItemStack =
		if (slot == 0) {
			itemStackForSale.shrink(amount)
			if (itemStackForSale.isEmpty)
				itemStackForSale = ItemStack.EMPTY
			setChanged()
			itemStackForSale
		} else {
			ItemStack.EMPTY
		}

	override fun stillValid(player: Player): Boolean = Container.stillValidBlockEntity(this, player)

	override fun loadAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
		super.loadAdditional(nbt, registryLookup)
		shopId = nbt.getUUID("shopId")
		owner = nbt.getUUID("owner")
		cost = nbt.getInt("cost")
		itemStackForSale = ItemStack.parse(registryLookup, nbt.getCompound("stackForSale")).orElse(ItemStack.EMPTY)
		linkedContainer = BlockPos.of(nbt.getLong("container"))
	}

	override fun saveAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
		super.saveAdditional(nbt, registryLookup)
		nbt.putUUID("shopId", shopId)
		nbt.putUUID("owner", owner)
		nbt.putInt("cost", cost)
		nbt.put("stackForSale", itemStackForSale.save(registryLookup, CompoundTag()))
		nbt.putLong("container", linkedContainer.asLong())
	}
}
