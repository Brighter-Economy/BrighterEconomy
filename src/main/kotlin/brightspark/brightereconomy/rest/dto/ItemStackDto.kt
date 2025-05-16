package brightspark.brightereconomy.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemStackDto(
	val item: String,
	val count: Int,
	val enchantments: List<EnchantmentDto>,
	val lore: String?
)
