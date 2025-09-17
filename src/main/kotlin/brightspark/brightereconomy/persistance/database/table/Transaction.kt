package brightspark.brightereconomy.persistance.database.table

import brightspark.brightereconomy.economy.Transaction
import brightspark.brightereconomy.economy.TransactionParticipants
import brightspark.brightereconomy.economy.TransactionType
import brightspark.brightereconomy.persistance.database.itemStackNullableWrapper
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import java.util.*

object TransactionTable : IdTable<UUID>("Transaction") {
	override val id: Column<EntityID<UUID>> = uuid("id").entityId()
	val type = varchar("type", 8).transform(
		wrap = TransactionType::valueOf,
		unwrap = TransactionType::name
	)
	val shopId = uuid("shopId").nullable()
	val participants = varchar("participants", 9).transform(
		wrap = TransactionParticipants::valueOf,
		unwrap = TransactionParticipants::name
	)
	val uuidFrom = uuid("uuidFrom").nullable()
	val uuidTo = uuid("uuidTo").nullable()
	val money = long("money")
	val itemPurchasedId = text("itemPurchasedId").nullable()
	val itemPurchasedCount = integer("itemPurchasedCount").nullable()
	val itemPurchasedNbt = text("itemPurchasedNbt").nullable()
	val timestamp = long("timestamp")
}

class TransactionEntity(id: EntityID<UUID>) : Entity<UUID>(id) {
	companion object : EntityClass<UUID, TransactionEntity>(TransactionTable)

	var type by TransactionTable.type
	var shopId by TransactionTable.shopId
	var participants by TransactionTable.participants
	var uuidFrom by TransactionTable.uuidFrom
	var uuidTo by TransactionTable.uuidTo
	var money by TransactionTable.money
	var itemPurchasedId by TransactionTable.itemPurchasedId
	var itemPurchasedCount by TransactionTable.itemPurchasedCount
	var itemPurchasedNbt by TransactionTable.itemPurchasedNbt
	var timestamp by TransactionTable.timestamp

	var itemPurchased by itemStackNullableWrapper(
		TransactionEntity::itemPurchasedId,
		TransactionEntity::itemPurchasedCount,
		TransactionEntity::itemPurchasedNbt
	)

	fun toTransaction(): Transaction = Transaction(
		id = id.value,
		type = type,
		shopId = shopId,
		participants = participants,
		uuidFrom = uuidFrom,
		uuidTo = uuidTo,
		money = money,
		itemPurchased = itemPurchased,
		timestamp = timestamp
	)
}
