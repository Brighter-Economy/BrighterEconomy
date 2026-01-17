package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.economy.PlayerAccountListener
import brightspark.brightereconomy.economy.Transaction
import brightspark.brightereconomy.persistance.database.EconomyDb
import java.util.*

interface EconomyStorage {
	companion object : StorageProvider<EconomyStorage> {
		override fun getStorage(): EconomyStorage = EconomyDb

		fun onPlayerAccountUpdated(account: PlayerAccount) = BrighterEconomy.SERVER.ifPresent { server ->
			server.playerList.players.asSequence()
				.map { it.containerMenu }
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
