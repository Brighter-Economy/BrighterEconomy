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

fun Enchantment.toDto(level: Int): EnchantmentDto =
	EnchantmentDto(Registries.ENCHANTMENT.getId(this).toString(), level)

fun ItemStack.toDto(): ItemStackDto =
	ItemStackDto(
		Registries.ITEM.getId(this.item).toString(),
		this.count,
		EnchantmentHelper.get(this).map { it.key.toDto(it.value) },
		this.nbt?.getString(ItemStack.LORE_KEY)
	)
