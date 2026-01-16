package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.dto.TransactionDto
import brightspark.brightereconomy.util.toDto
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Uuids
import java.util.*
import kotlin.jvm.optionals.getOrNull

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
			val id = nbt.get("id", Uuids.CODEC).get()
			val type = TransactionType.entries[nbt.getByte("type").map(Byte::toInt).get()]
			val shopId = if (type == TransactionType.PURCHASE) nbt.get("shopId", Uuids.CODEC).getOrNull() else null
			val participants = TransactionParticipants.entries[nbt.getByte("participants").map(Byte::toInt).get()]
			return Transaction(
				id = id,
				type = type,
				shopId = shopId,
				participants = participants,
				uuidFrom = if (participants.hasFrom) nbt.get("uuidFrom", Uuids.CODEC).getOrNull() else null,
				uuidTo = if (participants.hasTo) nbt.get("uuidTo", Uuids.CODEC).getOrNull() else null,
				money = nbt.getLong("money").get(),
				itemPurchased =
					if (nbt.getBoolean("hasItemPurchased").get())
						nbt.get("itemPurchased", ItemStack.OPTIONAL_CODEC).getOrNull()
					else
						null,
				timestamp = nbt.getLong("timestamp").get()
			)
		}
	}

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		put("id", Uuids.CODEC, id)
		putByte("type", this@Transaction.type.ordinal.toByte())
		shopId?.let { put("shopId", Uuids.CODEC, it) }
		putByte("participants", this@Transaction.participants.ordinal.toByte())
		uuidFrom?.let { put("uuidFrom", Uuids.CODEC, it) }
		uuidTo?.let { put("uuidTo", Uuids.CODEC, it) }
		putLong("money", money)
		putBoolean("hasItemPurchased", itemPurchased != null)
		itemPurchased?.let { put("itemPurchased", ItemStack.OPTIONAL_CODEC, it) }
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
