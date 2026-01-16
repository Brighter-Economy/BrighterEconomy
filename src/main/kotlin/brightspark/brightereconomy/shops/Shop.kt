@file:UseSerializers(UuidSerializer::class, BlockPosSerializer::class, ItemStackSerializer::class)

package brightspark.brightereconomy.shops

import brightspark.brightereconomy.rest.dto.ShopDto
import brightspark.brightereconomy.rest.serializer.BlockPosSerializer
import brightspark.brightereconomy.rest.serializer.ItemStackSerializer
import brightspark.brightereconomy.rest.serializer.UuidSerializer
import brightspark.brightereconomy.util.Util
import brightspark.brightereconomy.util.toDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Uuids
import net.minecraft.util.math.BlockPos
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Serializable
data class Shop(
	val id: UUID,
	val owner: UUID,
	val dimension: String,
	val position: BlockPos,
	val itemStack: ItemStack,
	val price: Int
) {
	constructor(nbt: NbtCompound) : this(
		nbt.get("id", Uuids.CODEC).get(),
		nbt.get("owner", Uuids.CODEC).get(),
		nbt.getString("dimension").get(),
		nbt.get("position", BlockPos.CODEC).get(),
		nbt.get("itemstack", ItemStack.OPTIONAL_CODEC).get(),
		nbt.getInt("price").get()
	)

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		put("id", Uuids.CODEC, id)
		put("owner", Uuids.CODEC, owner)
		putString("dimension", dimension)
		putLong("position", position.asLong())
		put("itemstack", ItemStack.OPTIONAL_CODEC, itemStack)
		putInt("price", price)
	}

	fun toDto(): ShopDto = ShopDto(
		id = id,
		ownerUuid = owner,
		ownerName = Util.getUsername(owner).getOrNull(),
		dimension = dimension,
		position = position.toDto(),
		itemStack = itemStack.toDto(),
		price = price
	)
}
