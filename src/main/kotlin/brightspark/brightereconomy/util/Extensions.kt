package brightspark.brightereconomy.util

import io.wispforest.owo.client.screens.OwoScreenHandler
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.reflect.KMutableProperty1

fun PlayerEntity.sendLiteralOverlayMessage(message: String, format: Formatting? = null): Unit =
	this.sendMessage(Text.literal(message).apply { format?.let { this.styled { it.withFormatting(format) } } }, true)

fun PlayerInventory.getSpaceFor(stack: ItemStack): Int {
	var count = 0
	for (slot in 0 until this.main.size) {
		val invStack = this.main[slot]
		if (invStack.isEmpty)
			count += stack.maxCount
		else if (ItemStack.areItemsEqual(invStack, stack))
			count += invStack.maxCount - invStack.count
	}
	return count
}

fun NbtCompound.getItemStack(key: String): ItemStack =
	ItemStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, this.getCompound(key))
		.resultOrPartial().map { it.first }.orElse(ItemStack.EMPTY)

fun ItemStack.toNbt(): NbtElement =
	ItemStack.OPTIONAL_CODEC.encode(this, NbtOps.INSTANCE, NbtCompound())
		.resultOrPartial().orElse(NbtCompound())

inline fun <reified V> OwoScreenHandler.property(value: V): SyncedProperty<V> =
	createProperty(V::class.java, value).apply { markDirty() }

inline fun <INST, reified V> OwoScreenHandler.property(
	instance: INST?,
	instanceProperty: KMutableProperty1<INST, V>,
	defaultValue: V
): SyncedProperty<V> = createProperty(
	V::class.java,
	instance?.let { instanceProperty.get(it) } ?: defaultValue
).apply {
	observe { value ->
		instance?.let { instanceProperty.set(it, value) }
	}
	markDirty()
}

inline fun <INST, reified V> OwoScreenHandler.property(
	instance: INST?,
	getter: (INST) -> V,
	crossinline setter: (INST, V) -> Unit,
	defaultValue: V
): SyncedProperty<V> = createProperty(
	V::class.java,
	instance?.let { getter(it) } ?: defaultValue
).apply {
	observe { value ->
		instance?.let { setter(it, value) }
	}
	markDirty()
}
