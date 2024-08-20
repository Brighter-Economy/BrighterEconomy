@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.serializer.UuidSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.NbtCompound
import java.util.*

@Serializable
data class Transaction private constructor(
	val type: TransactionType,
	val uuidFrom: UUID?,
	val uuidTo: UUID?,
	val money: Long,
	val timestamp: Long = System.currentTimeMillis()
) {
	companion object {
		fun of(uuidFrom: UUID?, uuidTo: UUID?, money: Long, timestamp: Long = System.currentTimeMillis()): Transaction =
			Transaction(TransactionType.fromUuids(uuidFrom, uuidTo), uuidFrom, uuidTo, money, timestamp)

		fun deserialize(nbt: NbtCompound): Transaction {
			val type = TransactionType.entries[nbt.getByte("type").toInt()]
			return Transaction(
				type,
				if (type.hasFrom) nbt.getUuid("uuidFrom") else null,
				if (type.hasTo) nbt.getUuid("uuidTo") else null,
				nbt.getLong("money"),
				nbt.getLong("timestamp")
			)
		}
	}

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		putByte("type", this@Transaction.type.ordinal.toByte())
		uuidFrom?.let { putUuid("uuidFrom", it) }
		uuidTo?.let { putUuid("uuidTo", it) }
		putLong("money", money)
		putLong("timestamp", timestamp)
	}
}
