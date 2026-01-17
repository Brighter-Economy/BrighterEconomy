package brightspark.brightereconomy.commands.permissions

import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

object FabricPermissionsApiSupport : Permissions {
	override fun require(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> =
		me.lucko.fabric.api.permissions.v0.Permissions.require(permission, defaultRequiredLevel)
}
