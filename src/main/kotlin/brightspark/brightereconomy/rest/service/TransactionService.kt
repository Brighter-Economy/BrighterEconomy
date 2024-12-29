package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.Transaction
import java.util.*

object TransactionService {
	fun getTransactions(limit: Int): List<Transaction> =
		getEconomyStateOrThrow { it.getTransactions().take(limit) }

	fun getTransactionsForPlayer(uuid: UUID, limit: Int): List<Transaction> =
		getEconomyStateOrThrow { it.getAccountTransactions(uuid).take(limit).toList() }
}
