package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.EconomyState
import io.ktor.server.plugins.*
import java.util.*

fun <T : Any> getEconomyStateOrThrow(mapper: (EconomyState) -> T): T =
	EconomyState.getOptional().map(mapper).orElseThrow { throw RuntimeException("MinecraftServer not available") }

fun parseUuid(uuidString: String): UUID = try {
	UUID.fromString(uuidString)
} catch (e: IllegalArgumentException) {
	throw BadRequestException("Invalid UUID")
}
