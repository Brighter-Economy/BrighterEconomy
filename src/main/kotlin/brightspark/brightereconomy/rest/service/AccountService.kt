package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.rest.dto.PlayerAccountDto
import net.minecraft.util.UserCache
import java.util.*

object AccountService {
	fun getAccounts(): List<PlayerAccountDto> {
		throwIfEconomyStateNull()
		val userCache = getUserCache()
		return EconomyService.getAccounts().asSequence()
			.map { it.toDto(userCache.getUsername(it.uuid)) }
			.toList()
	}

	fun getAccount(uuid: UUID): PlayerAccountDto {
		throwIfEconomyStateNull()
		return EconomyService.getAccount(uuid).toDto(getUserCache().getUsername(uuid))
	}

	fun setBalance(uuid: UUID, money: Long, loginUsername: String) {
		throwIfEconomyStateNull()
		EconomyService.set(uuid, money, loginUsername)
	}

	private fun getUserCache(): Optional<UserCache> =
		Optional.ofNullable(BrighterEconomy.SERVER.get().userCache)

	private fun Optional<UserCache>.getUsername(uuid: UUID): String =
		this.flatMap { it.getByUuid(uuid) }.map { it.name }.orElse("")
}
