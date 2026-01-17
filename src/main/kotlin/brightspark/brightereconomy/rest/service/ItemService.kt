package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import io.ktor.server.plugins.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

object ItemService {
	val localisedNameStorage: LocalisedNameStorage
		get() = LocalisedNameStorage.getStorage()

	fun getAllItemKeys(): List<String> =
		BuiltInRegistries.ITEM.registryKeySet().asSequence().map { it.location().toString() }.toList()

	fun getItemLocalisedName(itemId: String): String {
		throwIfItemLocalisedNameStateNull()
		val id = ResourceLocation.tryParse(itemId) ?: throw BadRequestException("Invalid item ID '$itemId'")
		return localisedNameStorage.getItemName(id) ?: throw NotFoundException("Localised name for '$itemId' not found")
	}
}
