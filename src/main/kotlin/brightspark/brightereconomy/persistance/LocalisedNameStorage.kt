package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.persistance.persistentstate.LocalisedNameState
import net.minecraft.util.Identifier

interface LocalisedNameStorage {
	companion object : BaseStorageProvider<LocalisedNameStorage>() {
		override fun getPersistentState(): LocalisedNameStorage = LocalisedNameState.get()
	}

	fun getItemName(id: Identifier): String?

	fun setItemName(id: Identifier, name: String)

	fun getEnchantmentName(id: Identifier): String?

	fun setEnchantmentName(id: Identifier, name: String)
}
