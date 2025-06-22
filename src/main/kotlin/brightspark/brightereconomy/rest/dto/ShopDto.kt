@file:UseSerializers(UuidSerializer::class)

package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.rest.serializer.UuidSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class ShopDto(
	val id: UUID,
	val ownerUuid: UUID,
	val ownerName: String?,
	val dimension: String,
	val position: PositionDto,
	val itemStack: ItemStackDto,
	val price: Int
)
