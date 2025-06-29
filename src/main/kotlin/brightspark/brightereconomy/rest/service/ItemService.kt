package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.persistance.ItemLocalisedNameStorage
import io.ktor.server.plugins.*
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object ItemService {
	val itemLocalisedNameStorage: ItemLocalisedNameStorage
		get() = ItemLocalisedNameStorage.getStorage()

	fun getAllItemKeys(): List<String> =
		Registries.ITEM.keys.asSequence().map { it.value.toString() }.toList()

	fun getItemLocalisedName(itemId: String): String {
		throwIfItemLocalisedNameStateNull()
		val id = Identifier.tryParse(itemId) ?: throw BadRequestException("Invalid item ID '$itemId'")
		return itemLocalisedNameStorage.getName(id) ?: throw NotFoundException("Localised name for '$itemId' not found")
	}
}
