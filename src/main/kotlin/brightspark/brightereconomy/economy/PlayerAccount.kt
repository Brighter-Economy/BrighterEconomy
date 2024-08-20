@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.dto.PlayerAccountDto
import brightspark.brightereconomy.rest.serializer.UuidSerializer
import brightspark.brightereconomy.util.Util
import io.wispforest.owo.network.serialization.PacketBufSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import java.util.*

@Serializable
data class PlayerAccount(
	val uuid: UUID,
	val locked: Boolean = false,
	val money: Long = 0
) {
	companion object {
		val SERIALIZER = PacketBufSerializer(
			{ buf, account -> account.writeBuf(buf) },
			{ buf -> PlayerAccount(buf) }
		)
	}

	constructor(nbt: NbtCompound) : this(
		nbt.getUuid("uuid"),
		nbt.getBoolean("locked"),
		nbt.getLong("money")
	)

	constructor(buf: PacketByteBuf) : this(
		buf.readUuid(),
		buf.readBoolean(),
		buf.readLong()
	)

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		putUuid("uuid", uuid)
		putBoolean("locked", locked)
		putLong("money", money)
	}

	fun writeBuf(buf: PacketByteBuf) {
		buf.apply {
			writeUuid(uuid)
			writeBoolean(locked)
			writeLong(money)
		}
	}

	fun toDto(username: String): PlayerAccountDto =
		PlayerAccountDto(uuid.toString(), username, locked, money, Util.formatMoney(money))
}
