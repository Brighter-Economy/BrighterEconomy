package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyState
import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.economy.PlayerAccountListener
import brightspark.brightereconomy.economy.Transaction
import brightspark.brightereconomy.persistance.StorageType.WORLD_NBT
import java.util.*

interface EconomyStorage {
	companion object : StorageProvider<EconomyStorage> {
		private var storage: EconomyStorage? = null

		override fun getStorage(): EconomyStorage = storage ?: run {
			storage = when (val type = BrighterEconomy.CONFIG.storageType()) {
				WORLD_NBT -> EconomyState.get()
				else -> error("No EconomyStorage for type $type")
			}
			storage!!
		}

		fun onPlayerAccountUpdated(account: PlayerAccount) = BrighterEconomy.SERVER.ifPresent { server ->
			server.playerManager.playerList.asSequence()
				.map { it.currentScreenHandler }
				.filter { it is PlayerAccountListener }
				.forEach { (it as PlayerAccountListener).handlePlayerAccountUpdate(account) }
		}
	}

	fun getAccounts(): Collection<PlayerAccount>

	fun getAccount(uuid: UUID): PlayerAccount

	fun updateAccount(uuid: UUID, accountConsumer: (PlayerAccount?) -> PlayerAccount): PlayerAccount

	fun getTransactions(): Sequence<Transaction>

	fun addTransaction(transaction: Transaction)
}
