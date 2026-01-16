package brightspark.brightereconomy.persistance.database

import brightspark.brightereconomy.persistance.database.table.AuthPasswordsEntity
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

// TODO: Change this later for proper encrypted password storage
object AuthPasswordsDb {
	fun hasPassword(username: String): Boolean =
		AuthPasswordsEntity.findById(username) != null

	fun setPassword(username: String, password: String) {
		transaction {
			updateOrCreate(AuthPasswordsEntity, username) { it.password = password }
		}
	}

	fun checkPassword(username: String, password: String): Boolean =
		AuthPasswordsEntity.findById(username)?.password == password
}
