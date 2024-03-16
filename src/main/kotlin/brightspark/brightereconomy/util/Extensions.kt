package brightspark.brightereconomy.util

import io.wispforest.owo.client.screens.OwoScreenHandler
import io.wispforest.owo.client.screens.SyncedProperty
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.reflect.KMutableProperty1

fun PlayerEntity.sendLiteralOverlayMessage(message: String, format: Formatting? = null): Unit =
	this.sendMessage(Text.literal(message).apply { format?.let { this.styled { it.withFormatting(format) } } }, true)

inline fun <INST, reified V> OwoScreenHandler.property(
	instance: Holder<INST?>,
	instanceProperty: KMutableProperty1<INST, V>,
	defaultValue: V
): SyncedProperty<V> = createProperty(
	V::class.java,
	instance.value?.let { instanceProperty.get(it) } ?: defaultValue
).apply {
	observe { value ->
		instance.value?.let { instanceProperty.set(it, value) }
	}
	markDirty()
}
