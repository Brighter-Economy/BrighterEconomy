package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.persistance.database.ShopTrackerDb
import brightspark.brightereconomy.shops.Shop
import java.util.*

interface ShopTrackerStorage {
	companion object : StorageProvider<ShopTrackerStorage> {
		override fun getStorage(): ShopTrackerStorage = ShopTrackerDb
	}

	fun getShops(): Collection<Shop>

	fun addShop(be: ShopBlockEntity)

	fun updateShop(id: UUID, shopUpdater: (Shop) -> Shop): Shop?

	fun removeShop(be: ShopBlockEntity)
}
