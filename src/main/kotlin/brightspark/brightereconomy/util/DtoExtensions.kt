package brightspark.brightereconomy.util

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

fun Enchantment.toDto(level: Int): EnchantmentDto = EnchantmentDto(
	id = Registries.ENCHANTMENT.getId(this).toString(),
	level = if (this.minLevel == 1 && this.maxLevel == 1) null else level
)

fun ItemStack.toDto(): ItemStackDto = ItemStackDto(
	item = Registries.ITEM.getId(this.item).toString(),
	count = this.count,
	customName = if (this.hasCustomName()) this.name.string else null,
	enchantments = EnchantmentHelper.get(this).map { it.key.toDto(it.value) },
	lore = this.nbt?.getString(ItemStack.LORE_KEY)
)
