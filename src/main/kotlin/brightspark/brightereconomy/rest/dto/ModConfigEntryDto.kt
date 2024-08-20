package brightspark.brightereconomy.rest.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ModConfigEntryDto<T : Any>(val key: String, @Contextual val value: T)
