package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import brightspark.brightereconomy.util.toDto
import kotlinx.serialization.Serializable
import net.minecraft.component.DataComponentTypes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries

@Serializable
data class ItemStackDto(
	val item: String,
	val count: Int,
	val name: String?,
	val customName: String?,
	val enchantments: List<EnchantmentDto>,
	val lore: String?
) {
	companion object {
		fun fromItemStack(stack: ItemStack): ItemStackDto {
			val id = Registries.ITEM.getId(stack.item)
			return ItemStackDto(
				item = id.toString(),
				count = stack.count,
				name = LocalisedNameStorage.getStorage().getItemName(id),
				customName = stack.customName?.string,
				enchantments = EnchantmentHelper.getEnchantments(stack)
					.let { itemEnchants -> itemEnchants.enchantments.map { it.toDto(itemEnchants.getLevel(it)) } },
				lore = stack.get(DataComponentTypes.LORE)?.lines?.joinToString("\n") { it.string }
			)
		}
	}
}
