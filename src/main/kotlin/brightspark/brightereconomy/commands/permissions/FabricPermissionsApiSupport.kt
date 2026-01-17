package brightspark.brightereconomy.commands.permissions

import net.minecraft.commands.CommandSourceStack
import java.util.function.Predicate

object FabricPermissionsApiSupport : Permissions {
	override fun require(permission: String, defaultRequiredLevel: Int): Predicate<CommandSourceStack> =
		me.lucko.fabric.api.permissions.v0.Permissions.require(permission, defaultRequiredLevel)
}
