package brightspark.brightereconomy.persistance.database

import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.persistance.ShopTrackerStorage
import brightspark.brightereconomy.persistance.database.table.ShopEntity
import brightspark.brightereconomy.shops.Shop
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

object ShopTrackerDb : ShopTrackerStorage {
	override fun getShops(): Collection<Shop> = transaction {
		ShopEntity.all().map { it.toShop() }
	}

	override fun addShop(be: ShopBlockEntity) {
		transaction {
			ShopEntity.new(be.shopId) {
				owner = be.owner
				dimension = be.world!!.dimensionEntry.idAsString
				position = be.pos
				itemStack = be.stack
				price = be.cost
			}
		}
	}

	override fun updateShop(
		id: UUID,
		shopUpdater: (Shop) -> Shop
	): Shop? = transaction {
		ShopEntity.findByIdAndUpdate(id) {
			val updatedShop = it.toShop().run { shopUpdater(this) }
			it.updateFromShop(updatedShop)
		}?.toShop()
	}

	override fun removeShop(be: ShopBlockEntity) {
		transaction {
			ShopEntity.findById(be.shopId)?.delete()
		}
	}
}
