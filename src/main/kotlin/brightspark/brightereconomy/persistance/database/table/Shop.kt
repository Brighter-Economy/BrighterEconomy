package brightspark.brightereconomy.persistance.database.table

import brightspark.brightereconomy.persistance.database.itemStackWrapper
import brightspark.brightereconomy.shops.Shop
import net.minecraft.util.math.BlockPos
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import java.util.*

object ShopTable : IdTable<UUID>("Shop") {
	override val id: Column<EntityID<UUID>> = uuid("id").entityId()
	val owner = uuid("owner")
	val dimension = text("dimension")
	val position = long("position").transform(
		wrap = BlockPos::fromLong,
		unwrap = BlockPos::asLong
	)
	val itemStackId = text("itemStackId")
	val itemStackCount = integer("itemStackCount")
	val itemStackNbt = text("itemStackNbt").nullable()
	val price = integer("price")
}

class ShopEntity(id: EntityID<UUID>) : Entity<UUID>(id) {
	companion object : EntityClass<UUID, ShopEntity>(ShopTable)

	var owner by ShopTable.owner
	var dimension by ShopTable.dimension
	var position: BlockPos by ShopTable.position
	var itemStackId by ShopTable.itemStackId
	var itemStackCount by ShopTable.itemStackCount
	var itemStackNbt by ShopTable.itemStackNbt
	var price by ShopTable.price

	var itemStack by itemStackWrapper(
		ShopEntity::itemStackId,
		ShopEntity::itemStackCount,
		ShopEntity::itemStackNbt
	)

	fun toShop(): Shop = Shop(
		id = id.value,
		owner = owner,
		dimension = dimension,
		position = position,
		itemStack = itemStack,
		price = price
	)

	fun updateFromShop(shop: Shop) {
		owner = shop.owner
		dimension = shop.dimension
		position = shop.position
		itemStack = shop.itemStack
		price = shop.price
	}
}
