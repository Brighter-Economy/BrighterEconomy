@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.rest.serializer.UuidSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class PlayerAccountDto(
	val uuid: UUID,
	val username: String,
	val locked: Boolean,
	val money: Long,
	val moneyFormatted: String
)
