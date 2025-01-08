package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.EconomyService

private fun mcNotAvailableException(cause: Throwable? = null): RuntimeException =
	cause?.let { RuntimeException("MinecraftServer not available", it) }
		?: RuntimeException("MinecraftServer not available")

fun throwIfEconomyStateNull() {
	try {
		EconomyService.storage
	} catch (e: Throwable) {
		throw mcNotAvailableException(e)
	}
}
