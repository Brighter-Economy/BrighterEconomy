package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.rest.dto.PlayerAccountDto
import net.minecraft.util.UserCache
import java.util.*

object AccountService {
	fun getAccounts(): List<PlayerAccountDto> = getEconomyStateOrThrow { state ->
		val userCache = getUserCache()
		state.getAccounts().asSequence()
			.map { it.toDto(userCache.getUsername(it.uuid)) }
			.toList()
	}

	fun getAccount(uuidString: String): PlayerAccountDto {
		val uuid = parseUuid(uuidString)
		return getEconomyStateOrThrow { state ->
			val userCache = getUserCache()
			state.getAccount(uuid).toDto(userCache.getUsername(uuid))
		}
	}

	private fun getUserCache(): Optional<UserCache> =
		Optional.ofNullable(BrighterEconomy.SERVER.get().userCache)

	private fun Optional<UserCache>.getUsername(uuid: UUID): String =
		this.flatMap { it.getByUuid(uuid) }.map { it.name }.orElse("")
}
