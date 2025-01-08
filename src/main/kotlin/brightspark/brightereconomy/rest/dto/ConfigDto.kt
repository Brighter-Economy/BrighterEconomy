package brightspark.brightereconomy.rest.dto

data class ConfigDto(
	val name: String,
	val value: String,
	val type: ConfigValueType,
	val possibleValues: List<String>?
)
