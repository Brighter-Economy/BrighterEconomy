package brightspark.brightereconomy.rest.serializer

import brightspark.brightereconomy.util.Util
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import kotlin.jvm.optionals.getOrNull

object ItemStackSerializer : KSerializer<ItemStack> {
	override val descriptor: SerialDescriptor = ItemStackSurrogate.serializer().descriptor

	override fun serialize(encoder: Encoder, value: ItemStack) {
		val item = BuiltInRegistries.ITEM.getKey(value.item).toString()
		val components = Util.componentsToJsonString(value.components).getOrNull()
		val surrogate = ItemStackSurrogate(item, value.count, components)
		encoder.encodeSerializableValue(ItemStackSurrogate.serializer(), surrogate)
	}

	override fun deserialize(decoder: Decoder): ItemStack {
		val surrogate = decoder.decodeSerializableValue(ItemStackSurrogate.serializer())
		val item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(surrogate.item))
		val stack = ItemStack(item, surrogate.count)
		if (!surrogate.components.isNullOrBlank()) {
			Util.jsonStringToComponents(surrogate.components).ifPresent { stack.applyComponents(it) }
		}
		return stack
	}
}

@Serializable
@SerialName("ItemStack")
private class ItemStackSurrogate(val item: String, val count: Int, val components: String?)
