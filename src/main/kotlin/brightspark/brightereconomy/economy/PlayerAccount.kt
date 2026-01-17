@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.economy

import brightspark.brightereconomy.rest.dto.PlayerAccountDto
import brightspark.brightereconomy.rest.serializer.UuidSerializer
import brightspark.brightereconomy.util.Util
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import java.util.*

@Serializable
data class PlayerAccount(
	val uuid: UUID,
	val locked: Boolean = false,
	val money: Long = 0
) {
//	companion object {
//		val SERIALIZER = PacketBufSerializer(
//			{ buf, account -> account.writeBuf(buf) },
//			{ buf -> PlayerAccount(buf) }
//		)
//	}

	val remainingTransferLimit: Int?
		get() = EconomyService.getRemainingTransferLimit(this)

	constructor(nbt: CompoundTag) : this(
		nbt.getUUID("uuid"),
		nbt.getBoolean("locked"),
		nbt.getLong("money")
	)

	constructor(buf: FriendlyByteBuf) : this(
		buf.readUUID(),
		buf.readBoolean(),
		buf.readLong()
	)

	fun writeNbt(nbt: CompoundTag): CompoundTag = nbt.apply {
		putUUID("uuid", uuid)
		putBoolean("locked", locked)
		putLong("money", money)
	}

	fun writeBuf(buf: FriendlyByteBuf) {
		buf.apply {
			writeUUID(uuid)
			writeBoolean(locked)
			writeLong(money)
		}
	}

	fun toDto(username: String): PlayerAccountDto =
		PlayerAccountDto(uuid, username, locked, money, Util.formatMoney(money))
}
