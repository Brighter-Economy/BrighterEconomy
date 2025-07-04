package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.shops.Shop
import brightspark.brightereconomy.persistance.persistentstate.ShopTrackerState
import java.util.*

interface ShopTrackerStorage {
	companion object : BaseStorageProvider<ShopTrackerStorage>() {
		override fun getPersistentState(): ShopTrackerStorage = ShopTrackerState.get()
	}

	fun getShops(): Collection<Shop>

	fun addShop(be: ShopBlockEntity)

	fun updateShop(id: UUID, shopUpdater: (Shop) -> Shop): Shop?

	fun removeShop(be: ShopBlockEntity)
}
