package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.rest.dto.ConfigDto
import brightspark.brightereconomy.rest.dto.ConfigValueType
import io.ktor.server.plugins.*
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.Option.Key

object ConfigService {
	fun getConfigs(): List<ConfigDto> = BrighterEconomy.CONFIG.allOptions().values.map(::optionToConfigDto)

	fun getConfig(key: String): ConfigDto = BrighterEconomy.CONFIG.optionForKey<Any>(Key(key))
		?.let(::optionToConfigDto)
		?: run { throw NotFoundException("Config '${key}' not found") }

	fun setConfig(key: String, value: String) {
		BrighterEconomy.CONFIG.optionForKey<Any>(Key(key))
			?.let { option ->
				option.set(
					when (val clazz = option.clazz()) {
						String::class.java -> value
						Boolean::class.java -> value.toBoolean()
						Int::class.java -> value.toInt()
						Long::class.java -> value.toLong()
						List::class.java -> value.trimStart('[').trimEnd(']').split(", ")
						else -> when {
							clazz.isEnum -> clazz.enumConstants.find { it.toString().equals(value, false) }
							else -> error("Config '${key}' value of type ${clazz.name} isn't supported")
						}
					}
				)
			}
			?: run { throw NotFoundException("Config '${key}' not found") }
	}

	fun optionToConfigDto(option: Option<*>): ConfigDto {
		val name = option.key().name()
		var value = option.value().toString()
		var possibleValues: List<String>? = null
		val type = when (val clazz = option.clazz()) {
			String::class.java -> ConfigValueType.STRING
			Boolean::class.java -> ConfigValueType.BOOLEAN
			Int::class.java -> ConfigValueType.INTEGER
			Long::class.java -> ConfigValueType.LONG
			List::class.java -> {
				val listValue = option.value() as List<*>
				value = when {
					listValue.isEmpty() -> "[]"
					listValue.first() is String ->
						listValue.joinToString(separator = ", ", prefix = "[", postfix = "]")
					else -> error("Config '$name' list value of type ${listValue::class.typeParameters.first().name} isn't supported")
				}
				ConfigValueType.LIST
			}
			else -> when {
				clazz.isEnum -> {
					possibleValues = clazz.enumConstants.map { it.toString() }
					ConfigValueType.ENUM
				}
				else -> error("Config '${name}' value of type ${clazz.name} isn't supported")
			}
		}
		return ConfigDto(name, value, type, possibleValues)
	}
}
