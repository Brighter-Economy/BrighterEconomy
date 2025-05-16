package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.StorageType.WORLD_NBT

abstract class BaseStorageProvider<T> : StorageProvider<T> {
	override fun getStorage(): T = when (val type = BrighterEconomy.CONFIG.storageType()) {
		WORLD_NBT -> getPersistentState()
		else -> error("No EconomyStorage for type $type")
	}

	abstract fun getPersistentState(): T
}
