package brightspark.brightereconomy.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayerAccountDto(
	val uuid: String,
	val username: String,
	val locked: Boolean,
	val money: Long,
	val moneyFormatted: String
)
