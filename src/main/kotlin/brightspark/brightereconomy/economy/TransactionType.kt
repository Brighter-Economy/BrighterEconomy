package brightspark.brightereconomy.economy

import java.util.*

enum class TransactionType(val hasTo: Boolean, val hasFrom: Boolean) {
	PLAYERS(true, true),
	TO_NULL(false, true),
	FROM_NULL(true, false);

	companion object {
		fun fromUuids(from: UUID?, to: UUID?): TransactionType =
			if (from != null && to != null) PLAYERS
			else if (from == null) FROM_NULL
			else TO_NULL
	}
}
