@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.UuidSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.NbtCompound
import java.util.*

@Serializable
data class Transaction(
	val uuidFrom: UUID?,
	val uuidTo: UUID?,
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
