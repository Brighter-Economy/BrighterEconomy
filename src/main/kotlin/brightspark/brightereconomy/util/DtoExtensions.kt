package brightspark.brightereconomy.util

import brightspark.brightereconomy.rest.dto.EnchantmentDto
import brightspark.brightereconomy.rest.dto.ItemStackDto
import brightspark.brightereconomy.rest.dto.PositionDto
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Holder
import net.minecraft.core.BlockPos

fun BlockPos.toDto(): PositionDto =
	PositionDto(this.x, this.y, this.z)

fun Holder<Enchantment>.toDto(level: Int): EnchantmentDto =
	EnchantmentDto.fromRegistryEntry(this, level)

fun ItemStack.toDto(): ItemStackDto =
	ItemStackDto.fromItemStack(this)
