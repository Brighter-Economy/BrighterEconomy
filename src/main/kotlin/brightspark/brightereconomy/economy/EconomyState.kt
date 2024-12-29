package brightspark.brightereconomy.economy

import brightspark.brightereconomy.BrighterEconomy
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*

class EconomyState : PersistentState {
	companion object {
		fun getOptional(): Optional<EconomyState> = BrighterEconomy.SERVER.map { get(it) }

		fun get(): EconomyState = getOptional().orElseThrow()

		fun get(server: MinecraftServer): EconomyState {
			val manager = server.getWorld(World.OVERWORLD)!!.persistentStateManager
			val state = manager.getOrCreate(::EconomyState, ::EconomyState, BrighterEconomy.MOD_ID)
			state.markDirty()
			return state
		}
	}

	private val accounts = mutableMapOf<UUID, PlayerAccount>()
	private val transactions = mutableListOf<Transaction>()
	// TODO: Cached transactions? e.g. last 5 for player

	constructor()

	constructor(nbt: NbtCompound) {
		readNbt(nbt)
	}

	fun getAccountUuids(): Set<UUID> = accounts.keys

	fun getAccounts(): Collection<PlayerAccount> = accounts.values

	fun getAccount(uuid: UUID): PlayerAccount = accounts.getOrElse(uuid) { PlayerAccount(uuid = uuid) }

	fun setMoney(uuid: UUID, money: Long, initiatorName: String? = null) {
		accounts.compute(uuid) { _, account ->
			account?.copy(money = money) ?: PlayerAccount(uuid = uuid, money = money)
		}.also {
			onPlayerAccountUpdated(it!!)
		}

		initiatorName?.let {
			BrighterEconomy.LOG.atInfo()
				.setMessage("Money set success {} to {} initiated by {}")
				.addArgument(uuid).addArgument(money).addArgument(initiatorName)
				.log()
		}
	}

	fun getTransactions(): List<Transaction> = transactions.toList()

	fun getAccountTransactions(uuid: UUID): Sequence<Transaction> =
		transactions.asSequence().filter { it.uuidTo == uuid || it.uuidFrom == uuid }

	private fun transactionTransfer(uuidFrom: UUID?, uuidTo: UUID?, money: Long) {
		transactions += Transaction.transfer(uuidFrom, uuidTo, money)
	}

	// TODO: Implement usage for shop purchases
	private fun transactionPurchase(uuidFrom: UUID?, uuidTo: UUID, money: Long, stack: ItemStack) {
		transactions += Transaction.purchase(uuidFrom, uuidTo, money, stack)
	}

	// TODO: Implement usage for commands
	private fun transactionModify(uuid: UUID, money: Long) {
		if (money == 0.toLong()) return
		val from = if (money < 0) uuid else null
		val to = if (money > 0) uuid else null
		transactions += Transaction.modify(from, to, money)
	}

	// TODO: Implement usage for commands
	private fun transactionSet(uuid: UUID, money: Long) {
		val diff = money - getAccount(uuid).money
		if (diff == 0.toLong()) return
		val from = if (diff < 0) uuid else null
		val to = if (diff > 0) uuid else null
		transactions += Transaction.modify(from, to, diff)
	}

	fun simulateExchange(uuidFrom: UUID?, uuidTo: UUID?, money: Long): TransactionExchangeResult {
		if (uuidFrom == null && uuidTo == null)
			throw IllegalArgumentException("Can't exchange money between two null account UUIDs!")

		val from = uuidFrom?.let { getAccount(it) }
		val to = uuidTo?.let { getAccount(it) }
		return validateExchange(from, to, money)
	}

	fun exchange(uuidFrom: UUID?, uuidTo: UUID?, money: Long, initiatorName: String): TransactionExchangeResult {
		if (uuidFrom == null && uuidTo == null)
			throw IllegalArgumentException("Can't exchange money between two null account UUIDs!")

		BrighterEconomy.LOG.atInfo()
			.setMessage("Attempting to exchange {} from {} to {} initiated by {}")
			.addArgument(money).addArgument(uuidFrom).addArgument(uuidTo).addArgument(initiatorName)
			.log()

		val from = uuidFrom?.let { getAccount(it) }
		val to = uuidTo?.let { getAccount(it) }
		val result = validateExchange(from, to, money)
		if (result != TransactionExchangeResult.SUCCESS) {
			BrighterEconomy.LOG.atInfo()
				.setMessage("Exchange failure {} from {} to {} initiated by {} due to {}")
				.addArgument(money).addArgument(uuidFrom).addArgument(uuidTo).addArgument(initiatorName)
				.addArgument(result)
				.log()
			return result
		}

		from?.let { setMoney(it.uuid, it.money - money) }
		to?.let { setMoney(it.uuid, it.money + money) }

		BrighterEconomy.LOG.atInfo()
			.setMessage("Exchange success {} from {} to {} initiated by {}")
			.addArgument(money).addArgument(uuidFrom).addArgument(uuidTo).addArgument(initiatorName)
			.log()

		return TransactionExchangeResult.SUCCESS
	}

	fun transfer(uuidFrom: UUID?, uuidTo: UUID?, money: Long, initiatorName: String): TransactionExchangeResult {
		val transactionResult = exchange(uuidFrom, uuidTo, money, initiatorName)
		if (transactionResult == TransactionExchangeResult.SUCCESS)
			transactionTransfer(uuidFrom, uuidTo, money)
		return transactionResult
	}

	private fun validateExchange(from: PlayerAccount?, to: PlayerAccount?, money: Long): TransactionExchangeResult {
		from?.takeIf { it.locked }?.let {
			BrighterEconomy.LOG.atWarn().setMessage("Exchange failed due to {} locked").addArgument(it.uuid).log()
			return TransactionExchangeResult.FROM_LOCKED
		}
		to?.takeIf { it.locked }?.let {
			BrighterEconomy.LOG.atWarn().setMessage("Exchange failed due to {} locked").addArgument(it.uuid).log()
			return TransactionExchangeResult.TO_LOCKED
		}
		from?.takeIf { it.money < money }?.let {
			BrighterEconomy.LOG.atWarn()
				.setMessage("Exchange failed due to {} insufficient money ({})")
				.addArgument(it.uuid).addArgument(it.money)
				.log()
			return TransactionExchangeResult.INSUFFICIENT_MONEY
		}
		to?.takeIf { Long.MAX_VALUE - it.money < money }?.let {
			BrighterEconomy.LOG.atWarn()
				.setMessage("Exchange failed due to {} overflow money ({})")
				.addArgument(it.uuid).addArgument(it.money)
				.log()
			return TransactionExchangeResult.OVERFLOW_MONEY
		}

		return TransactionExchangeResult.SUCCESS
	}

	private fun setAccountLock(uuid: UUID, locked: Boolean) {
		accounts.compute(uuid) { _, account ->
			account?.copy(locked = locked) ?: PlayerAccount(uuid = uuid, locked = locked)
		}.also {
			onPlayerAccountUpdated(it!!)
		}
	}

	fun lockAccount(uuid: UUID) {
		setAccountLock(uuid, true)
		BrighterEconomy.LOG.atInfo().setMessage("Locked account {}").addArgument(uuid).log()
	}

	fun unlockAccount(uuid: UUID) {
		setAccountLock(uuid, false)
		BrighterEconomy.LOG.atInfo().setMessage("Unlocked account {}").addArgument(uuid).log()
	}

	private fun onPlayerAccountUpdated(account: PlayerAccount) = BrighterEconomy.SERVER.ifPresent { server ->
		server.playerManager.playerList.asSequence()
			.map { it.currentScreenHandler }
			.filter { it is PlayerAccountListener }
			.forEach { (it as PlayerAccountListener).handlePlayerAccountUpdate(account) }
	}

	private fun readNbt(nbt: NbtCompound) {
		accounts.clear()
		nbt.getList("accounts", NbtElement.COMPOUND_TYPE.toInt()).forEach {
			val account = PlayerAccount(it as NbtCompound)
			accounts[account.uuid] = account
		}
		transactions.clear()
		nbt.getList("transactions", NbtElement.COMPOUND_TYPE.toInt()).forEach {
			transactions += Transaction.deserialize(it as NbtCompound)
		}
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		put("accounts", NbtList().apply { accounts.values.forEach { add(it.writeNbt(NbtCompound())) } })
		put("transactions", NbtList().apply { transactions.forEach { add(it.writeNbt(NbtCompound())) } })
	}
}
