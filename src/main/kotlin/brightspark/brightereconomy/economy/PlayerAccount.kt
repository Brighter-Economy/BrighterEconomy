@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.UuidSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.NbtCompound
import java.util.*

@Serializable
data class PlayerAccount(
	val uuid: UUID,
	val locked: Boolean = false,
	val money: Long = 0
) {
	constructor(nbt: NbtCompound) : this(
		nbt.getUuid("uuid"),
		nbt.getBoolean("locked"),
		nbt.getLong("money")
	)

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		putUuid("uuid", uuid)
		putBoolean("locked", locked)
		putLong("money", money)
	}
}
