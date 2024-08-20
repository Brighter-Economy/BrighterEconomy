package brightspark.brightereconomy.economy

import net.minecraft.text.Text

enum class TransactionExchangeResult(val langKey: String) {
	SUCCESS("brightereconomy.transaction.result.success"),
	INSUFFICIENT_MONEY("brightereconomy.transaction.result.insufficient"),
	OVERFLOW_MONEY("brightereconomy.transaction.result.overflow"),
	FROM_LOCKED("brightereconomy.transaction.result.from_locked"),
	TO_LOCKED("brightereconomy.transaction.result.to_locked");

	val text: Text
		get() = Text.translatable(langKey)
}
