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
import net.minecraft.util.math.BlockPos
import java.util.*

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
		nbt.getUuid("id"),
		nbt.getUuid("owner"),
		nbt.getString("dimension"),
		BlockPos.fromLong(nbt.getLong("position")),
		ItemStack.fromNbt(nbt.getCompound("itemstack")),
		nbt.getInt("price")
	)

	fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		putUuid("id", id)
		putUuid("owner", owner)
		putString("dimension", dimension)
		putLong("position", position.asLong())
		put("itemstack", itemStack.writeNbt(NbtCompound()))
		putInt("price", price)
	}

	fun toDto(): ShopDto = ShopDto(
		id = id.toString(),
		ownerUuid = owner.toString(),
		ownerName = Util.getUsername(owner),
		dimension = dimension,
		position = position.toDto(),
		item = itemStack.toDto(),
		price = price
	)
}
