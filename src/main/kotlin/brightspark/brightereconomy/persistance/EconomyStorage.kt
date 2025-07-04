package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.persistentstate.EconomyState
import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.economy.PlayerAccountListener
import brightspark.brightereconomy.economy.Transaction
import java.util.*

interface EconomyStorage {
	companion object : BaseStorageProvider<EconomyStorage>() {
		override fun getPersistentState(): EconomyStorage = EconomyState.get()

		fun onPlayerAccountUpdated(account: PlayerAccount) = BrighterEconomy.SERVER.ifPresent { server ->
			server.playerManager.playerList.asSequence()
				.map { it.currentScreenHandler }
				.filter { it is PlayerAccountListener }
				.forEach { (it as PlayerAccountListener).handlePlayerAccountUpdate(account) }
		}
	}

	fun getAccounts(): Collection<PlayerAccount>

	fun getAccount(uuid: UUID): PlayerAccount

	fun updateAccount(uuid: UUID, accountUpdater: (PlayerAccount?) -> PlayerAccount): PlayerAccount

	fun getTransactions(): Sequence<Transaction>

	fun addTransaction(transaction: Transaction)
}
