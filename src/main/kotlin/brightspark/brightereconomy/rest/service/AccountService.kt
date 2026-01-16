package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.rest.dto.PlayerAccountDto
import net.minecraft.util.UserCache
import java.util.*

object AccountService {
	fun getAccounts(): List<PlayerAccountDto> {
		val userCache = getUserCache()
		return EconomyService.getAccounts()
			.map { it.toDto(userCache.getUsername(it.uuid)) }
	}

	fun getAccount(uuid: UUID): PlayerAccountDto {
		return EconomyService.getAccount(uuid).toDto(getUserCache().getUsername(uuid))
	}

	fun setBalance(uuid: UUID, money: Long, loginUsername: String) {
		EconomyService.set(uuid, money, loginUsername)
	}

	fun setLock(uuid: UUID, shouldLock: Boolean, loginUsername: String) {
		if (shouldLock)
			EconomyService.lockAccount(uuid, loginUsername)
		else
			EconomyService.unlockAccount(uuid, loginUsername)
	}

	private fun getUserCache(): Optional<UserCache> =
		Optional.ofNullable(BrighterEconomy.SERVER.get().userCache)

	private fun Optional<UserCache>.getUsername(uuid: UUID): String =
		this.flatMap { it.getByUuid(uuid) }.map { it.name }.orElse("")
}
