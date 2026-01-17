package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import kotlinx.serialization.Serializable
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.core.Holder

@Serializable
data class EnchantmentDto(val id: String, val level: Int?, val name: String?) {
	companion object {
		fun fromRegistryEntry(registryEntry: Holder<Enchantment>, level: Int): EnchantmentDto {
			val id = registryEntry.unwrapKey().get().location()
			val enchantment = registryEntry.value()
			return EnchantmentDto(
				id = id.toString(),
				level = if (enchantment.minLevel == 1 && enchantment.maxLevel == 1) null else level,
				name = LocalisedNameStorage.getStorage().getEnchantmentName(id)
			)
		}
	}
}
