package brightspark.brightereconomy.economy

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.EconomyStorage
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*

class EconomyState : PersistentState, EconomyStorage {
	companion object {
		fun get(): EconomyState = BrighterEconomy.SERVER.map { get(it) }.orElseThrow()

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

	override fun getAccounts(): Collection<PlayerAccount> = accounts.values

	override fun getAccount(uuid: UUID): PlayerAccount = accounts.getOrElse(uuid) { PlayerAccount(uuid = uuid) }

	override fun updateAccount(uuid: UUID, accountConsumer: (PlayerAccount?) -> PlayerAccount): PlayerAccount =
		accounts.compute(uuid) { _, account -> accountConsumer(account) }!!

	override fun getTransactions(): Sequence<Transaction> = transactions.asSequence()

	override fun addTransaction(transaction: Transaction) {
		transactions += transaction
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
