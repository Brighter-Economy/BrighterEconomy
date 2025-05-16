package brightspark.brightereconomy.shops

import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.persistance.ShopTrackerStorage
import java.util.*

object ShopTrackerService {
	val storage: ShopTrackerStorage
		get() = ShopTrackerStorage.getStorage()

	fun getShops(): Collection<Shop> = storage.getShops()

	fun addShop(be: ShopBlockEntity): Unit = storage.addShop(be)

	fun updateShop(id: UUID, shopUpdater: (Shop) -> Shop): Shop? = storage.updateShop(id, shopUpdater)

	fun removeShop(be: ShopBlockEntity): Unit = storage.removeShop(be)
}
