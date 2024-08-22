package brightspark.brightereconomy.economy

import java.util.*

enum class TransactionParticipants(val hasTo: Boolean, val hasFrom: Boolean) {
	PLAYERS(true, true),
	TO_NULL(false, true),
	FROM_NULL(true, false);

	companion object {
		fun fromUuids(from: UUID?, to: UUID?): TransactionParticipants =
			if (from != null && to != null) PLAYERS
			else if (from == null) FROM_NULL
			else TO_NULL
	}
}
