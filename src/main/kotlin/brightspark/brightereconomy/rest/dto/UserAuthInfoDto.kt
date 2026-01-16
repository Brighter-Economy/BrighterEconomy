package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.BrighterEconomy
import java.util.*

data class UserAuthInfoDto(
	val username: String,
	val playerUuid: UUID,
	val type: Type
) {
	enum class Type {
		PLAYER, ADMIN
	}

	constructor(username: String, playerUuid: UUID) : this(
		username,
		playerUuid,
		if (BrighterEconomy.CONFIG.adminUsernames().contains(username)) Type.ADMIN else Type.PLAYER
	)
}
