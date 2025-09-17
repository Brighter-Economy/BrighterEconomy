package brightspark.brightereconomy.persistance.database

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.database.table.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DbConnection {
	private var connected = false

	fun connect() {
		if (connected) return

		val dbType = BrighterEconomy.CONFIG.dbType()
		BrighterEconomy.LOG.atInfo().setMessage("Setting up connection to {} DB").addArgument(dbType).log()
		Database.connect(dbType.dataSource())
		connected = true

		transaction {
			SchemaUtils.create(EnchantmentNameTable, ItemNameTable, PlayerAccountTable, ShopTable, TransactionTable)
		}
	}
}
