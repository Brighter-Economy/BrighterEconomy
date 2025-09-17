package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.persistance.database.LocalisedNameDb
import net.minecraft.util.Identifier

interface LocalisedNameStorage {
	companion object : StorageProvider<LocalisedNameStorage> {
		override fun getStorage(): LocalisedNameStorage = LocalisedNameDb
	}

	fun getItemName(id: Identifier): String?

	fun setItemNames(names: Sequence<Pair<String, String>>)

	fun getEnchantmentName(id: Identifier): String?

	fun setEnchantmentNames(names: Sequence<Pair<String, String>>)
}
