package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.server.plugins.*
import io.wispforest.owo.config.Option.Key
import net.minecraft.util.JsonHelper

object ConfigService {
	fun getConfigsString(): String {
		val json = JsonObject()
		BrighterEconomy.CONFIG.allOptions().values.forEach { option ->
			val name = option.key().name()
			when (val value = option.value()) {
				is Boolean -> json.addProperty(name, value)
				is Number -> json.addProperty(name, value)
				is String -> json.addProperty(name, value)
				is List<*> -> {
					when {
						value.isEmpty() -> json.add(name, JsonArray())
						value.first() is String -> json.add(name, JsonArray().also { array ->
							value.forEach { array.add(it as String) }
						})
						else -> BrighterEconomy.LOG.warn(
							"Config '{}' list value of type {} isn't supported",
							name, value::class.typeParameters.first().name
						)
					}
				}
				else -> BrighterEconomy.LOG.warn(
					"Config '{}' value of type {} isn't supported",
					name, value::class.qualifiedName
				)
			}
		}
		return JsonHelper.toSortedString(json)
	}

	fun setConfigs(configsString: String) {
		JsonHelper.deserialize(configsString).entrySet().forEach { (key, value) ->
			BrighterEconomy.CONFIG.optionForKey<Any>(Key(key))?.set(value)
		}
	}

	fun setConfig(key: String, value: String) {
		BrighterEconomy.CONFIG.optionForKey<Any>(Key(key))?.set(value) ?: run {
			throw NotFoundException("Config '${key}' not found")
		}
	}
}
