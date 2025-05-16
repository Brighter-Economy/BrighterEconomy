package brightspark.brightereconomy.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShopDto(
	val id: String,
	val ownerUuid: String,
	val ownerName: String?,
	val dimension: String,
	val position: PositionDto,
	val itemStack: ItemStackDto,
	val price: Int
)
