package brightspark.brightereconomy.economy

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.TransactionExchangeResult.*
import brightspark.brightereconomy.persistance.EconomyStorage
import net.minecraft.item.ItemStack
import java.util.*

object EconomyService {
	val storage: EconomyStorage
		get() = EconomyStorage.getStorage()

	fun getAccounts(): Collection<PlayerAccount> = storage.getAccounts()

	fun getAccount(uuid: UUID): PlayerAccount = storage.getAccount(uuid)

	fun simulateExchange(uuidFrom: UUID?, uuidTo: UUID?, money: Long): TransactionExchangeResult {
		if (uuidFrom == null && uuidTo == null)
			throw IllegalArgumentException("Can't exchange money between two null account UUIDs!")

		val state = storage
		val from = uuidFrom?.let { state.getAccount(it) }
		val to = uuidTo?.let { state.getAccount(it) }
		return validateExchange(from, to, money)
	}

	private fun exchange(
		uuidFrom: UUID?,
		uuidTo: UUID?,
		money: Long,
		initiatorName: String
	): TransactionExchangeResult {
		if (uuidFrom == null && uuidTo == null)
			throw IllegalArgumentException("Can't exchange money between two null account UUIDs!")

		BrighterEconomy.LOG.atInfo()
			.setMessage("Attempting to exchange {} from {} to {} initiated by {}")
			.addArgument(money).addArgument(uuidFrom).addArgument(uuidTo).addArgument(initiatorName)
			.log()

		val from = uuidFrom?.let { getAccount(it) }
		val to = uuidTo?.let { getAccount(it) }
		val result = validateExchange(from, to, money)
		if (result != SUCCESS) {
			BrighterEconomy.LOG.atInfo()
				.setMessage("Exchange failure {} from {} to {} initiated by {} due to {}")
				.addArgument(money).addArgument(uuidFrom).addArgument(uuidTo).addArgument(initiatorName)
				.addArgument(result)
				.log()
			return result
		}

		from?.let { setMoney(it.uuid, it.money - money, initiatorName) }
		to?.let { setMoney(it.uuid, it.money + money, initiatorName) }

		BrighterEconomy.LOG.atInfo()
			.setMessage("Exchange success {} from {} to {} initiated by {}")
			.addArgument(money).addArgument(uuidFrom).addArgument(uuidTo).addArgument(initiatorName)
			.log()

		return SUCCESS
	}

	fun transfer(
		uuidFrom: UUID?,
		uuidTo: UUID?,
		money: Long,
		initiatorName: String
	): TransactionExchangeResult =
		exchange(uuidFrom, uuidTo, money, initiatorName).also {
			if (it == SUCCESS) transactionTransfer(uuidFrom, uuidTo, money)
		}

	fun purchase(
		uuidFrom: UUID?,
		uuidTo: UUID,
		money: Long,
		stack: ItemStack,
		initiatorName: String
	): TransactionExchangeResult =
		exchange(uuidFrom, uuidTo, money, initiatorName).also {
			if (it == SUCCESS) transactionPurchase(uuidFrom, uuidTo, money, stack)
		}

	fun modify(uuid: UUID, add: Boolean, money: Long, initiatorName: String): TransactionExchangeResult {
		val from = if (add) null else uuid
		val to = if (add) uuid else null
		return exchange(from, to, money, initiatorName).also {
			if (it == SUCCESS) transactionModify(uuid, money)
		}
	}

	fun set(uuid: UUID, money: Long, initiatorName: String) {
		val diff = money - getAccount(uuid).money
		setMoney(uuid, money, initiatorName)
		transactionModify(uuid, diff)
	}

	fun lockAccount(uuid: UUID) {
		setAccountLock(uuid, true)
		BrighterEconomy.LOG.atInfo().setMessage("Locked account {}").addArgument(uuid).log()
	}

	fun unlockAccount(uuid: UUID) {
		setAccountLock(uuid, false)
		BrighterEconomy.LOG.atInfo().setMessage("Unlocked account {}").addArgument(uuid).log()
	}

	fun getTransactions(): Sequence<Transaction> = storage.getTransactions()

	private fun validateExchange(from: PlayerAccount?, to: PlayerAccount?, money: Long): TransactionExchangeResult {
		from?.takeIf { it.locked }?.let {
			BrighterEconomy.LOG.atWarn().setMessage("Exchange failed due to {} locked").addArgument(it.uuid).log()
			return FROM_LOCKED
		}
		to?.takeIf { it.locked }?.let {
			BrighterEconomy.LOG.atWarn().setMessage("Exchange failed due to {} locked").addArgument(it.uuid).log()
			return TO_LOCKED
		}
		from?.takeIf { it.money < money }?.let {
			BrighterEconomy.LOG.atWarn()
				.setMessage("Exchange failed due to {} insufficient money ({})")
				.addArgument(it.uuid).addArgument(it.money)
				.log()
			return INSUFFICIENT_MONEY
		}
		to?.takeIf { Long.MAX_VALUE - it.money < money }?.let {
			BrighterEconomy.LOG.atWarn()
				.setMessage("Exchange failed due to {} overflow money ({})")
				.addArgument(it.uuid).addArgument(it.money)
				.log()
			return OVERFLOW_MONEY
		}

		return SUCCESS
	}

	private fun transactionTransfer(uuidFrom: UUID?, uuidTo: UUID?, money: Long) {
		storage.addTransaction(Transaction.transfer(uuidFrom, uuidTo, money))
	}

	private fun transactionPurchase(uuidFrom: UUID?, uuidTo: UUID, money: Long, stack: ItemStack) {
		storage.addTransaction(Transaction.purchase(uuidFrom, uuidTo, money, stack))
	}

	private fun transactionModify(uuid: UUID, money: Long) {
		if (money == 0.toLong()) return
		val from = if (money < 0) uuid else null
		val to = if (money > 0) uuid else null
		storage.addTransaction(Transaction.modify(from, to, money))
	}

	private fun setMoney(uuid: UUID, money: Long, initiatorName: String?) {
		val state = storage
		val account = state.updateAccount(uuid) {
			it?.copy(money = money) ?: PlayerAccount(uuid = uuid, money = money)
		}
		EconomyStorage.onPlayerAccountUpdated(account)
		initiatorName?.let {
			BrighterEconomy.LOG.atInfo()
				.setMessage("Money set success {} to {} initiated by {}")
				.addArgument(uuid).addArgument(money).addArgument(initiatorName)
				.log()
		}
	}

	private fun setAccountLock(uuid: UUID, locked: Boolean) {
		val state = storage
		val account = state.updateAccount(uuid) {
			it?.copy(locked = locked) ?: PlayerAccount(uuid = uuid, locked = locked)
		}
		EconomyStorage.onPlayerAccountUpdated(account)
	}
}
