package brightspark.brightereconomy.util

import io.wispforest.owo.client.screens.OwoScreenHandler
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import kotlin.reflect.KMutableProperty1

fun Player.sendLiteralOverlayMessage(message: String, format: ChatFormatting? = null): Unit =
	this.displayClientMessage(Component.literal(message).apply { format?.let { this.withStyle { it.applyFormat(format) } } }, true)

fun Inventory.getSpaceFor(stack: ItemStack): Int {
	var count = 0
	for (slot in 0 until this.items.size) {
		val invStack = this.items[slot]
		if (invStack.isEmpty)
			count += stack.maxStackSize
		else if (ItemStack.isSameItem(invStack, stack))
			count += invStack.maxStackSize - invStack.count
	}
	return count
}

fun CompoundTag.getItemStack(key: String): ItemStack =
	ItemStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, this.getCompound(key))
		.resultOrPartial().map { it.first }.orElse(ItemStack.EMPTY)

fun ItemStack.toNbt(): Tag =
	ItemStack.OPTIONAL_CODEC.encode(this, NbtOps.INSTANCE, CompoundTag())
		.resultOrPartial().orElse(CompoundTag())

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
