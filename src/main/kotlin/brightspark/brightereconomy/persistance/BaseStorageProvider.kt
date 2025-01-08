package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.StorageType.WORLD_NBT

abstract class BaseStorageProvider<T> : StorageProvider<T> {
	private var storage: T? = null

	override fun getStorage(): T = storage ?: run {
		storage = when (val type = BrighterEconomy.CONFIG.storageType()) {
			WORLD_NBT -> createPersistentState()
			else -> error("No EconomyStorage for type $type")
		}
		storage!!
	}

	abstract fun createPersistentState(): T
}
