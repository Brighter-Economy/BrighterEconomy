package brightspark.brightereconomy.economy

enum class TransactionExchangeResult {
	SUCCESS,
	INSUFFICIENT_MONEY,
	OVERFLOW_MONEY,
	FROM_LOCKED,
	TO_LOCKED
}
