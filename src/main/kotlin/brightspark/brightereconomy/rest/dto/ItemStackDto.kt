package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import brightspark.brightereconomy.util.toDto
import kotlinx.serialization.Serializable
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries

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
			val id = BuiltInRegistries.ITEM.getKey(stack.item)
			return ItemStackDto(
				item = id.toString(),
				count = stack.count,
				name = LocalisedNameStorage.getStorage().getItemName(id),
				customName = stack.get(DataComponents.CUSTOM_NAME)?.string,
				enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack)
					.let { itemEnchants -> itemEnchants.keySet().map { it.toDto(itemEnchants.getLevel(it)) } },
				lore = stack.get(DataComponents.LORE)?.lines?.joinToString("\n") { it.string }
			)
		}
	}
}
