package brightspark.brightereconomy.rest.service

import net.minecraft.registry.Registries

object ItemService {
	fun getAllItemKeys(): List<String> =
		Registries.ITEM.keys.asSequence().map { it.value.toString() }.toList()
}
