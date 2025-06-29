package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.persistance.miscstate.ItemLocalisedNameState
import net.minecraft.util.Identifier

interface ItemLocalisedNameStorage {
	companion object : BaseStorageProvider<ItemLocalisedNameStorage>() {
		override fun getPersistentState(): ItemLocalisedNameStorage = ItemLocalisedNameState.get()
	}

	fun getName(id: Identifier): String?

	fun setName(id: Identifier, name: String)
}
