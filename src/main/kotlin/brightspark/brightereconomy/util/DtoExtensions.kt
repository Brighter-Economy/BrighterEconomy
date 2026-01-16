package brightspark.brightereconomy.util

import brightspark.brightereconomy.rest.dto.EnchantmentDto
import brightspark.brightereconomy.rest.dto.ItemStackDto
import brightspark.brightereconomy.rest.dto.PositionDto
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.math.BlockPos

fun BlockPos.toDto(): PositionDto =
	PositionDto(this.x, this.y, this.z)

fun RegistryEntry<Enchantment>.toDto(level: Int): EnchantmentDto =
	EnchantmentDto.fromRegistryEntry(this, level)

fun ItemStack.toDto(): ItemStackDto =
	ItemStackDto.fromItemStack(this)
