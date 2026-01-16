package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy

private fun mcNotAvailableException(cause: Throwable? = null): RuntimeException =
	cause?.let { RuntimeException("MinecraftServer not available", it) }
		?: RuntimeException("MinecraftServer not available")

fun throwIfServerNotAvailable() {
	if (BrighterEconomy.SERVER.isEmpty)
		throw mcNotAvailableException()
}
