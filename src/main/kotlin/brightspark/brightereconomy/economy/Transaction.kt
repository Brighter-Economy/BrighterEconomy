package brightspark.brightereconomy.economy

import net.minecraft.nbt.NbtCompound
import java.util.*

data class Transaction(
	val uuidFrom: UUID,
	val uuidTo: UUID,
	val money: Long,
	val timestamp: Long = System.currentTimeMillis()
) {
	constructor(nbt: NbtCompound) : this(
		nbt.getUuid("uuidFrom"),
		nbt.getUuid("uuidTo"),
		nbt.getLong("money"),
		nbt.getLong("timestamp")
	)

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		putUuid("uuidFrom", uuidFrom)
		putUuid("uuidTo", uuidTo)
		putLong("money", money)
		putLong("timestamp", timestamp)
	}
}
