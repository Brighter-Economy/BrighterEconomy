package brightspark.brightereconomy.persistance

interface StorageProvider<T> {
	fun getStorage(): T
}
