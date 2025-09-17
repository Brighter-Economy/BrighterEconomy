package brightspark.brightereconomy.persistance.database.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

object ItemNameTable : IdTable<String>("ItemName") {
	override val id: Column<EntityID<String>> = varchar("id", 255).entityId()
	val name = varchar("name", 255)
}

class ItemNameEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, ItemNameEntity>(ItemNameTable)

	var name by ItemNameTable.name
}
