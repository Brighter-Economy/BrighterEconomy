package brightspark.brightereconomy.util

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import brightspark.brightereconomy.rest.dto.EnchantmentDto
import brightspark.brightereconomy.rest.dto.ItemStackDto
import brightspark.brightereconomy.rest.dto.PositionDto
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos

fun BlockPos.toDto(): PositionDto =
	PositionDto(this.x, this.y, this.z)

fun Enchantment.toDto(level: Int): EnchantmentDto {
	val id = Registries.ENCHANTMENT.getId(this)!!
	return EnchantmentDto(
		id = id.toString(),
		level = if (this.minLevel == 1 && this.maxLevel == 1) null else level,
		name = LocalisedNameStorage.getStorage().getEnchantmentName(id)
	)
}

fun ItemStack.toDto(): ItemStackDto {
	val id = Registries.ITEM.getId(this.item)
	return ItemStackDto(
		item = id.toString(),
		count = this.count,
		name = LocalisedNameStorage.getStorage().getItemName(id),
		customName = if (this.hasCustomName()) this.name.string else null,
		enchantments = EnchantmentHelper.get(this).map { it.key.toDto(it.value) },
		lore = this.nbt?.getString(ItemStack.LORE_KEY)
	)
}
