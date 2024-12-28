package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.Transaction

object TransactionService {
	fun getTransactions(): List<Transaction> = getEconomyStateOrThrow { state ->
		state.getTransactions().toList()
	}

	fun getTransactionsForPlayer(uuidString: String): List<Transaction> {
		val uuid = parseUuid(uuidString)
		return getEconomyStateOrThrow { it.getAccountTransactions(uuid) }
	}
}
