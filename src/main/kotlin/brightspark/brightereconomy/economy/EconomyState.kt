package brightspark.brightereconomy.economy

import brightspark.brightereconomy.persistance.EconomyStorage
import brightspark.brightereconomy.persistance.PersistentStateProvider
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.world.PersistentState
import java.util.*

class EconomyState : PersistentState, EconomyStorage {
	companion object : PersistentStateProvider<EconomyState>("economy", ::EconomyState, ::EconomyState)

	private val accounts = mutableMapOf<UUID, PlayerAccount>()
	private val transactions = mutableListOf<Transaction>()
	// TODO: Cached transactions? e.g. last 5 for player

	constructor()

	constructor(nbt: NbtCompound) {
		readNbt(nbt)
	}

	override fun getAccounts(): Collection<PlayerAccount> = accounts.values

	override fun getAccount(uuid: UUID): PlayerAccount = accounts.getOrElse(uuid) { PlayerAccount(uuid = uuid) }

	override fun updateAccount(uuid: UUID, accountUpdater: (PlayerAccount?) -> PlayerAccount): PlayerAccount =
		accounts.compute(uuid) { _, account -> accountUpdater(account) }!!.also { markDirty() }

	override fun getTransactions(): Sequence<Transaction> = transactions.asSequence()

	override fun addTransaction(transaction: Transaction) {
		transactions += transaction
		markDirty()
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
