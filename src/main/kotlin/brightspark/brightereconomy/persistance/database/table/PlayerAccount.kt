package brightspark.brightereconomy.persistance.database.table

import brightspark.brightereconomy.economy.PlayerAccount
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import java.util.*

object PlayerAccountTable : IdTable<UUID>("PlayerAccount") {
	override val id: Column<EntityID<UUID>> = uuid("id").entityId()
	val locked = bool("locked").default(false)
	val money = long("money").default(0)
}

class PlayerAccountEntity(id: EntityID<UUID>) : Entity<UUID>(id) {
	companion object : EntityClass<UUID, PlayerAccountEntity>(PlayerAccountTable)

	var locked by PlayerAccountTable.locked
	var money by PlayerAccountTable.money

	fun toPlayerAccount(): PlayerAccount = PlayerAccount(
		uuid = id.value,
		locked = locked,
		money = money
	)

	fun updateFromPlayerAccount(playerAccount: PlayerAccount) {
		locked = playerAccount.locked
		money = playerAccount.money
	}
}
