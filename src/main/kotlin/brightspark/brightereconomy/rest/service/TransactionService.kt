package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.economy.Transaction
import brightspark.brightereconomy.rest.dto.Sort
import brightspark.brightereconomy.rest.dto.Sort.ASC
import brightspark.brightereconomy.rest.dto.TransactionDto
import brightspark.brightereconomy.util.Util
import java.util.*
import kotlin.jvm.optionals.getOrNull

object TransactionService {
	fun getTransactions(limit: Int, sort: Sort): List<TransactionDto> {
		return EconomyService.getTransactions()
			.sortTransactions(sort)
			.take(limit)
			.map(::transactionToDto)
			.toList()
	}

	fun getTransactionsForPlayer(uuid: UUID, limit: Int, sort: Sort): List<TransactionDto> {
		return EconomyService.getTransactions()
			.filter { it.uuidTo == uuid || it.uuidFrom == uuid }
			.sortTransactions(sort)
			.take(limit)
			.map(::transactionToDto)
			.toList()
	}

	private fun Sequence<Transaction>.sortTransactions(sort: Sort): Sequence<Transaction> =
		if (sort == ASC) this.sortedBy { it.timestamp } else this.sortedByDescending { it.timestamp }

	private fun transactionToDto(transaction: Transaction): TransactionDto {
		val nameFrom = transaction.uuidFrom?.let { Util.getUsername(it).getOrNull() }
		val nameTo = transaction.uuidTo?.let { Util.getUsername(it).getOrNull() }
		return transaction.toDto(nameFrom, nameTo)
	}
}
