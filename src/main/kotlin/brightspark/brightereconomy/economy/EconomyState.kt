package brightspark.brightereconomy.economy

import brightspark.brightereconomy.BrighterEconomy
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

	fun getTransactions(): Sequence<Transaction> = transactions.asSequence()

	fun addTransaction(transaction: Transaction) {
		transactions += transaction
	}

	fun setAccountLock(uuid: UUID, locked: Boolean) {
		accounts.compute(uuid) { _, account ->
			account?.copy(locked = locked) ?: PlayerAccount(uuid = uuid, locked = locked)
		}.also {
			onPlayerAccountUpdated(it!!)
		}
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
