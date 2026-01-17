package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.dto.TransactionDto
import brightspark.brightereconomy.util.getItemStack
import brightspark.brightereconomy.util.toDto
import brightspark.brightereconomy.util.toNbt
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import java.util.*

data class Transaction(
	val id: UUID = UUID.randomUUID(),
	val type: TransactionType,
	val shopId: UUID? = null,
	val participants: TransactionParticipants,
	val uuidFrom: UUID?,
	val uuidTo: UUID?,
	val money: Long,
	val itemPurchased: ItemStack? = null,
	val timestamp: Long = System.currentTimeMillis()
) {
	companion object {
		fun transfer(uuidFrom: UUID?, uuidTo: UUID?, money: Long): Transaction = Transaction(
			type = TransactionType.TRANSFER,
			participants = TransactionParticipants.fromUuids(uuidFrom, uuidTo),
			uuidFrom = uuidFrom,
			uuidTo = uuidTo,
			money = money
		)

		fun purchase(shopId: UUID, uuidFrom: UUID?, uuidTo: UUID?, money: Long, itemPurchased: ItemStack): Transaction =
			Transaction(
				type = TransactionType.PURCHASE,
				shopId = shopId,
				participants = TransactionParticipants.fromUuids(uuidFrom, uuidTo),
				uuidFrom = uuidFrom,
				uuidTo = uuidTo,
				money = money,
				itemPurchased = itemPurchased
			)

		fun modify(uuidFrom: UUID?, uuidTo: UUID?, money: Long): Transaction = Transaction(
			type = TransactionType.MODIFY,
			participants = TransactionParticipants.fromUuids(uuidFrom, uuidTo),
			uuidFrom = uuidFrom,
			uuidTo = uuidTo,
			money = money
		)

		fun deserialize(nbt: NbtCompound): Transaction {
			val id = nbt.getUuid("id")
			val type = TransactionType.entries[nbt.getByte("type").toInt()]
			val shopId = if (type == TransactionType.PURCHASE) nbt.getUuid("shopId") else null
			val participants = TransactionParticipants.entries[nbt.getByte("participants").toInt()]
			return Transaction(
				id = id,
				type = type,
				shopId = shopId,
				participants = participants,
				uuidFrom = if (participants.hasFrom) nbt.getUuid("uuidFrom") else null,
				uuidTo = if (participants.hasTo) nbt.getUuid("uuidTo") else null,
				money = nbt.getLong("money"),
				itemPurchased = if (nbt.getBoolean("hasItemPurchased")) nbt.getItemStack("itemPurchased") else null,
				timestamp = nbt.getLong("timestamp")
			)
		}
	}

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		putUuid("id", id)
		putByte("type", this@Transaction.type.ordinal.toByte())
		shopId?.let { putUuid("shopId", it) }
		putByte("participants", this@Transaction.participants.ordinal.toByte())
		uuidFrom?.let { putUuid("uuidFrom", it) }
		uuidTo?.let { putUuid("uuidTo", it) }
		putLong("money", money)
		putBoolean("hasItemPurchased", itemPurchased != null)
		itemPurchased?.let { put("itemPurchased", it.toNbt()) }
		putLong("timestamp", timestamp)
	}

	fun toDto(nameFrom: String?, nameTo: String?): TransactionDto = TransactionDto(
		id = id,
		type = type,
		shopId = shopId,
		participants = participants,
		uuidFrom = uuidFrom,
		uuidTo = uuidTo,
		nameFrom = nameFrom,
		nameTo = nameTo,
		money = money,
		itemStack = itemPurchased?.toDto(),
		timestamp = timestamp
	)
}
