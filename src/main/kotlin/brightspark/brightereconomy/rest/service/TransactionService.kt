package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.Transaction
import java.util.*

object TransactionService {
	fun getTransactions(limit: Int): List<Transaction> =
		getEconomyStateOrThrow { it.getTransactions().take(limit).toList() }

	fun getTransactionsForPlayer(uuid: UUID, limit: Int): List<Transaction> = getEconomyStateOrThrow { state ->
		state.getTransactions().filter { it.uuidTo == uuid || it.uuidFrom == uuid }.take(limit).toList()
	}
}
