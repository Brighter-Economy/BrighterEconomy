@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.dto.PlayerAccountDto
import brightspark.brightereconomy.rest.serializer.UuidSerializer
import brightspark.brightereconomy.util.Util
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Uuids
import java.util.*

@Serializable
data class PlayerAccount(
	val uuid: UUID,
	val locked: Boolean = false,
	val money: Long = 0
) {
	val remainingTransferLimit: Int?
		get() = EconomyService.getRemainingTransferLimit(this)

	constructor(nbt: NbtCompound) : this(
		nbt.get("uuid", Uuids.CODEC).get(),
		nbt.getBoolean("locked").get(),
		nbt.getLong("money").get()
	)

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		put("uuid", Uuids.CODEC, uuid)
		putBoolean("locked", locked)
		putLong("money", money)
	}

	fun toDto(username: String): PlayerAccountDto =
		PlayerAccountDto(uuid, username, locked, money, Util.formatMoney(money))
}
