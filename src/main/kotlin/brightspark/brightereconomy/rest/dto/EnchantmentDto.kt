package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import kotlinx.serialization.Serializable
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.entry.RegistryEntry

@Serializable
data class EnchantmentDto(val id: String, val level: Int?, val name: String?) {
	companion object {
		fun fromRegistryEntry(registryEntry: RegistryEntry<Enchantment>, level: Int): EnchantmentDto {
			val id = registryEntry.key.get().value
			val enchantment = registryEntry.value()
			return EnchantmentDto(
				id = id.toString(),
				level = if (enchantment.minLevel == 1 && enchantment.maxLevel == 1) null else level,
				name = LocalisedNameStorage.getStorage().getEnchantmentName(id)
			)
		}
	}
}
