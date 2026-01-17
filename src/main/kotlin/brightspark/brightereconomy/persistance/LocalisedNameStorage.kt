package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.persistance.database.LocalisedNameDb
import net.minecraft.resources.ResourceLocation

interface LocalisedNameStorage {
	companion object : StorageProvider<LocalisedNameStorage> {
		override fun getStorage(): LocalisedNameStorage = LocalisedNameDb
	}

	fun getItemName(id: ResourceLocation): String?

	fun setItemNames(names: Sequence<Pair<String, String>>)

	fun getEnchantmentName(id: ResourceLocation): String?

	fun setEnchantmentNames(names: Sequence<Pair<String, String>>)
}
