package brightspark.brightereconomy.persistance.database

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import brightspark.brightereconomy.persistance.database.table.EnchantmentNameEntity
import brightspark.brightereconomy.persistance.database.table.ItemNameEntity
import net.minecraft.resources.ResourceLocation
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object LocalisedNameDb : LocalisedNameStorage {
	override fun getItemName(id: ResourceLocation): String? = transaction {
		ItemNameEntity.findById(id.toString())?.name
	}

	override fun setItemNames(names: Sequence<Pair<String, String>>) {
		transaction {
			names.forEach { (id, name) ->
				updateOrCreate(ItemNameEntity, id) { it.name = name }
			}
		}
	}

	override fun getEnchantmentName(id: ResourceLocation): String? = transaction {
		EnchantmentNameEntity.findById(id.toString())?.name
	}

	override fun setEnchantmentNames(names: Sequence<Pair<String, String>>) {
		transaction {
			names.forEach { (id, name) ->
				updateOrCreate(EnchantmentNameEntity, id) { it.name = name }
			}
		}
	}
}
