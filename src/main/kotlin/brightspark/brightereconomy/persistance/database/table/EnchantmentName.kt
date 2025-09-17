package brightspark.brightereconomy.persistance.database.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

object EnchantmentNameTable : IdTable<String>("EnchantmentName") {
	override val id: Column<EntityID<String>> = varchar("id", 255).entityId()
	val name = varchar("name", 255)
}

class EnchantmentNameEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, EnchantmentNameEntity>(EnchantmentNameTable)

	var name by EnchantmentNameTable.name
}
