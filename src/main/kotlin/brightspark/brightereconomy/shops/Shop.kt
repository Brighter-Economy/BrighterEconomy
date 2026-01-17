@file:UseSerializers(UuidSerializer::class, BlockPosSerializer::class, ItemStackSerializer::class)

package brightspark.brightereconomy.shops

import brightspark.brightereconomy.rest.dto.ShopDto
import brightspark.brightereconomy.rest.serializer.BlockPosSerializer
import brightspark.brightereconomy.rest.serializer.ItemStackSerializer
import brightspark.brightereconomy.rest.serializer.UuidSerializer
import brightspark.brightereconomy.util.Util
import brightspark.brightereconomy.util.getItemStack
import brightspark.brightereconomy.util.toDto
import brightspark.brightereconomy.util.toNbt
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.BlockPos
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
	constructor(nbt: CompoundTag) : this(
		nbt.getUUID("id"),
		nbt.getUUID("owner"),
		nbt.getString("dimension"),
		BlockPos.of(nbt.getLong("position")),
		nbt.getItemStack("itemstack"),
		nbt.getInt("price")
	)

	fun writeNbt(nbt: CompoundTag): CompoundTag = nbt.apply {
		putUUID("id", this@Shop.id)
		putUUID("owner", owner)
		putString("dimension", dimension)
		putLong("position", position.asLong())
		put("itemstack", itemStack.toNbt())
		putInt("price", price)
	}

	fun toDto(): ShopDto = ShopDto(
		id = id,
		ownerUuid = owner,
		ownerName = Util.getUsername(owner),
		dimension = dimension,
		position = position.toDto(),
		itemStack = itemStack.toDto(),
		price = price
	)
}
