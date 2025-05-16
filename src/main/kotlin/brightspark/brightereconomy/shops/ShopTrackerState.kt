package brightspark.brightereconomy.shops

import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.persistance.PersistentStateProvider
import brightspark.brightereconomy.persistance.ShopTrackerStorage
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.world.PersistentState
import java.util.*

class ShopTrackerState : PersistentState, ShopTrackerStorage {
	companion object : PersistentStateProvider<ShopTrackerState>("shop-tracker", ::ShopTrackerState, ::ShopTrackerState)

	private val shops = mutableMapOf<UUID, Shop>()

	constructor()

	constructor(nbt: NbtCompound) {
		readNbt(nbt)
	}

	override fun getShops(): Collection<Shop> = shops.values

	override fun addShop(be: ShopBlockEntity) {
		shops[be.shopId] = Shop(
			id = be.shopId,
			owner = be.owner,
			dimension = be.world!!.dimensionKey.value.toString(),
			position = be.pos,
			itemStack = be.stack,
			price = be.cost
		)
	}

	override fun updateShop(id: UUID, shopUpdater: (Shop) -> Shop): Shop? =
		shops.computeIfPresent(id) { _, shop -> shopUpdater(shop) }

	override fun removeShop(be: ShopBlockEntity) {
		shops.remove(be.shopId)
	}

	private fun readNbt(nbt: NbtCompound) {
		shops.clear()
		nbt.getList("shops", NbtElement.COMPOUND_TYPE.toInt()).forEach {
			val shop = Shop(it as NbtCompound)
			shops[shop.id] = shop
		}
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		put("shops", NbtList().apply { shops.values.forEach { add(it.writeNbt(NbtCompound())) } })
	}
}
