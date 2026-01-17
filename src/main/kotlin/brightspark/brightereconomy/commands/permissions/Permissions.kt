package brightspark.brightereconomy.commands.permissions

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

interface Permissions {
	companion object : Permissions {
		private val fabricLoader = FabricLoader.getInstance()

		override fun require(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> {
			if (fabricLoader.isModLoaded("fabric-permissions-api-v0"))
				return FabricPermissionsApiSupport.require(permission, defaultRequiredLevel)

			return defaultRequire(permission, defaultRequiredLevel)
		}

		private fun defaultRequire(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> =
			{ it.hasPermissionLevel(defaultRequiredLevel) }
	}

	fun require(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> =
		defaultRequire(permission, defaultRequiredLevel)
}
