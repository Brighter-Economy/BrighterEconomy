package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.EconomyState
import io.ktor.server.plugins.*
import java.util.*

private val mcNotAvailableException: RuntimeException
	get() = RuntimeException("MinecraftServer not available")

fun throwIfEconomyStateNull() {
	if (EconomyState.getOptional().isEmpty)
		throw mcNotAvailableException
}

fun <T : Any> getEconomyStateOrThrow(mapper: (EconomyState) -> T): T =
	EconomyState.getOptional().map(mapper).orElseThrow { throw mcNotAvailableException }

fun parseUuid(uuidString: String): UUID = try {
	UUID.fromString(uuidString)
} catch (e: IllegalArgumentException) {
	throw BadRequestException("Invalid UUID")
}
