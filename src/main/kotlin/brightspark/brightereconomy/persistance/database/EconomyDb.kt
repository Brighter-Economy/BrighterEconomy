package brightspark.brightereconomy.persistance.database

import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.economy.Transaction
import brightspark.brightereconomy.persistance.EconomyStorage
import brightspark.brightereconomy.persistance.database.table.PlayerAccountEntity
import brightspark.brightereconomy.persistance.database.table.TransactionEntity
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

object EconomyDb : EconomyStorage {
	override fun getAccounts(): Collection<PlayerAccount> = transaction {
		PlayerAccountEntity.all().map { it.toPlayerAccount() }
	}

	override fun getAccount(uuid: UUID): PlayerAccount = transaction {
		findOrCreate(PlayerAccountEntity, uuid).toPlayerAccount()
	}

	override fun updateAccount(
		uuid: UUID,
		accountUpdater: (PlayerAccount?) -> PlayerAccount
	): PlayerAccount = transaction {
		updateOrCreate(PlayerAccountEntity, uuid) {
			val updatedAccount = it.toPlayerAccount().run { accountUpdater(this) }
			it.updateFromPlayerAccount(updatedAccount)
		}.toPlayerAccount()
	}

	override fun getTransactions(): Sequence<Transaction> = transaction {
		TransactionEntity.all().asSequence().map { it.toTransaction() }
	}

	override fun addTransaction(transaction: Transaction) {
		transaction {
			TransactionEntity.new(transaction.id) {
				type = transaction.type
				shopId = transaction.shopId
				participants = transaction.participants
				uuidFrom = transaction.uuidFrom
				uuidTo = transaction.uuidTo
				money = transaction.money
				itemPurchased = transaction.itemPurchased
				timestamp = transaction.timestamp
			}
		}
	}
}
