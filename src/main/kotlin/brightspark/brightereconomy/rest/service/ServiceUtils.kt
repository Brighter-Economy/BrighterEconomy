package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.shops.ShopTrackerService

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

fun throwIfShopTrackerStateNull() {
	try {
		ShopTrackerService.storage
	} catch (e: Throwable) {
		throw mcNotAvailableException(e)
	}
}

fun throwIfItemLocalisedNameStateNull() {
	try {
		ItemService.localisedNameStorage
	} catch (e: Throwable) {
		throw mcNotAvailableException(e)
	}
}
