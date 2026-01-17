package brightspark.brightereconomy.persistance.database

import net.minecraft.world.item.ItemStack
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class EntityItemStackDelegate<T : Any>(
	val itemId: KMutableProperty1<T, String>,
	val itemCount: KMutableProperty1<T, Int>,
	val itemNbt: KMutableProperty1<T, String?>
) : ReadWriteProperty<T, ItemStack> {
	override fun getValue(thisRef: T, property: KProperty<*>): ItemStack =
		itemStackFromDbParts(itemId.get(thisRef), itemCount.get(thisRef), itemNbt.get(thisRef))

	override fun setValue(thisRef: T, property: KProperty<*>, value: ItemStack) {
		value.toDbParts().let {
			itemId.set(thisRef, it.first)
			itemCount.set(thisRef, it.second)
			itemNbt.set(thisRef, it.third)
		}
	}
}

class EntityItemStackNullableDelegate<T : Any>(
	val itemId: KMutableProperty1<T, String?>,
	val itemCount: KMutableProperty1<T, Int?>,
	val itemNbt: KMutableProperty1<T, String?>
) : ReadWriteProperty<T, ItemStack?> {
	override fun getValue(thisRef: T, property: KProperty<*>): ItemStack? {
		val idValue = itemId.get(thisRef)
		val countValue = itemCount.get(thisRef)
		return if (idValue == null || countValue == null)
			null
		else
			itemStackFromDbParts(idValue, countValue, itemNbt.get(thisRef))
	}

	override fun setValue(thisRef: T, property: KProperty<*>, value: ItemStack?) {
		value?.toDbParts().let {
			itemId.set(thisRef, it?.first)
			itemCount.set(thisRef, it?.second)
			itemNbt.set(thisRef, it?.third)
		}
	}
}

fun <T : Any> itemStackWrapper(
	itemId: KMutableProperty1<T, String>,
	itemCount: KMutableProperty1<T, Int>,
	itemNbt: KMutableProperty1<T, String?>
) = EntityItemStackDelegate(itemId, itemCount, itemNbt)

fun <T : Any> itemStackNullableWrapper(
	itemId: KMutableProperty1<T, String?>,
	itemCount: KMutableProperty1<T, Int?>,
	itemNbt: KMutableProperty1<T, String?>
) = EntityItemStackNullableDelegate(itemId, itemCount, itemNbt)
