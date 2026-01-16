package brightspark.brightereconomy.persistance.database.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

object AuthPasswordsTable : IdTable<String>("AuthPasswords") {
	override val id: Column<EntityID<String>> = varchar("username", 255).entityId()
	var password = varchar("password", 255)
}

class AuthPasswordsEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, AuthPasswordsEntity>(AuthPasswordsTable)

	var password by AuthPasswordsTable.password
}
