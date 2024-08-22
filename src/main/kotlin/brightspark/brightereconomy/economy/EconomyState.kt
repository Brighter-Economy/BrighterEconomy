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
	private val transactions = mutableMapOf<UUID, MutableList<Transaction>>()

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

	fun getTransactions(): Set<Transaction> = transactions.values.flatten().toSet()

	fun getAccountTransactions(uuid: UUID): List<Transaction> = transactions[uuid] ?: emptyList()

	private fun addTransaction(uuid: UUID, transaction: Transaction) {
		transactions.compute(uuid) { _, list -> (list ?: mutableListOf()).apply { this += transaction } }
	}

	private fun transactionTransfer(uuidFrom: UUID?, uuidTo: UUID?, money: Long) {
		Transaction.of(type = TransactionType.TRANSFER, uuidFrom = uuidFrom, uuidTo = uuidTo, money = money)
			.let { transaction ->
				uuidFrom?.let { addTransaction(it, transaction) }
				uuidTo?.let { addTransaction(it, transaction) }
			}
	}

	// TODO: Implement usage for shop purchases
	private fun transactionPurchase(uuidFrom: UUID?, uuidTo: UUID, money: Long, stack: ItemStack) {
		addTransaction(
			uuidTo,
			Transaction.of(
				type = TransactionType.PURCHASE,
				uuidFrom = uuidFrom,
				uuidTo = uuidTo,
				money = money,
				itemPurchased = stack
			)
		)
		uuidFrom?.let {
			addTransaction(
				it,
				Transaction.of(
					type = TransactionType.SALE,
					uuidFrom = it,
					uuidTo = uuidTo,
					money = money,
					itemPurchased = stack
				)
			)
		}
	}

	// TODO: Implement usage for commands
	private fun transactionModify(uuid: UUID, money: Long) {
		if (money == 0.toLong()) return
		addTransaction(
			uuid,
			Transaction.of(
				type = TransactionType.MODIFY,
				uuidFrom = if (money < 0) uuid else null,
				uuidTo = if (money > 0) uuid else null,
				money = money
			)
		)
	}

	// TODO: Implement usage for commands
	private fun transactionSet(uuid: UUID, money: Long) {
		val diff = money - getAccount(uuid).money
		addTransaction(
			uuid,
			Transaction.of(
				type = TransactionType.MODIFY,
				uuidFrom = if (diff < 0) uuid else null,
				uuidTo = if (diff > 0) uuid else null,
				money = diff
			)
		)
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
		if (result != TransactionExchangeResult.SUCCESS)
			return result

		from?.let { setMoney(it.uuid, it.money - money) }
		to?.let { setMoney(it.uuid, it.money + money) }
		transactionTransfer(from?.uuid, to?.uuid, money)

		BrighterEconomy.LOG.atInfo()
			.setMessage("Exchange success {} from {} to {} initiated by {}")
			.addArgument(money).addArgument(uuidFrom).addArgument(uuidTo).addArgument(initiatorName)
			.log()
		return TransactionExchangeResult.SUCCESS
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
		nbt.getList("transactions", NbtElement.COMPOUND_TYPE.toInt()).forEach { transactionListEntryNbt ->
			val uuid = (transactionListEntryNbt as NbtCompound).getUuid("uuid")
			val list = transactionListEntryNbt.getList("list", NbtElement.COMPOUND_TYPE.toInt())
				.mapTo(mutableListOf()) { Transaction.deserialize(it as NbtCompound) }
			transactions[uuid] = list
		}
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		put("accounts", NbtList().apply { accounts.values.forEach { add(it.writeNbt(NbtCompound())) } })
		put("transactions", NbtList().apply {
			transactions.forEach { (uuid, list) ->
				add(NbtCompound().apply {
					putUuid("uuid", uuid)
					put("list", NbtList().apply {
						list.forEach { add(it.writeNbt(NbtCompound())) }
					})
				})
			}
		})
	}
}
