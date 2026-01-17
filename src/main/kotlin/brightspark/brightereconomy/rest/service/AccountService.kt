package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.rest.dto.PlayerAccountDto
import net.minecraft.server.players.GameProfileCache
import java.util.*

object AccountService {
	fun getAccounts(): List<PlayerAccountDto> {
		throwIfEconomyStateNull()
		val userCache = getUserCache()
		return EconomyService.getAccounts()
			.map { it.toDto(userCache.getUsername(it.uuid)) }
	}

	fun getAccount(uuid: UUID): PlayerAccountDto {
		throwIfEconomyStateNull()
		return EconomyService.getAccount(uuid).toDto(getUserCache().getUsername(uuid))
	}

	fun setBalance(uuid: UUID, money: Long, loginUsername: String) {
		throwIfEconomyStateNull()
		EconomyService.set(uuid, money, loginUsername)
	}

	private fun getUserCache(): Optional<GameProfileCache> =
		Optional.ofNullable(BrighterEconomy.SERVER.get().profileCache)

	private fun Optional<GameProfileCache>.getUsername(uuid: UUID): String =
		this.flatMap { it.get(uuid) }.map { it.name }.orElse("")
}
