package brightspark.brightereconomy.rest.serializer

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object ItemStackSerializer : KSerializer<ItemStack> {
	override val descriptor: SerialDescriptor = ItemStackSurrogate.serializer().descriptor

	override fun serialize(encoder: Encoder, value: ItemStack) {
		val item = Registries.ITEM.getId(value.item).toString()
		val nbt = value.nbt?.let { stackNbt ->
			NbtCompound.CODEC.encodeStart(JsonOps.INSTANCE, stackNbt)
				.resultOrPartial { error -> throw RuntimeException(error) }
				.get().asString
		}
		val surrogate = ItemStackSurrogate(item, value.count, nbt)
		encoder.encodeSerializableValue(ItemStackSurrogate.serializer(), surrogate)
	}

	override fun deserialize(decoder: Decoder): ItemStack {
		val surrogate = decoder.decodeSerializableValue(ItemStackSurrogate.serializer())
		val item = Registries.ITEM.get(Identifier(surrogate.item))
		val stack = ItemStack(item, surrogate.count)
		if (!surrogate.nbt.isNullOrBlank()) {
			stack.nbt = NbtCompound.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(surrogate.nbt).asJsonObject)
				.resultOrPartial { error -> throw RuntimeException(error) }
				.get()
		}
		return stack
	}
}

@Serializable
@SerialName("ItemStack")
private class ItemStackSurrogate(val item: String, val count: Int, val nbt: String?)
