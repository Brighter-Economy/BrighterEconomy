package brightspark.brightereconomy.commands

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.commands.permissions.Permissions
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.world.entity.player.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import java.util.*

abstract class Command(
	name: String,
	builderBlock: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit
) {
	companion object {
		private const val COMMAND_PERM = "command.${BrighterEconomy.MOD_ID}"

		/**
		 * Returns a literal node that redirects its execution to the given [destination] node.
		 *
		 * [Based on the example in Velocity here](https://github.com/PaperMC/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38)
		 *
		 * [Brigadier issue](https://github.com/Mojang/brigadier/issues/46)
		 */
		fun buildRedirect(
			alias: String,
			destination: LiteralCommandNode<CommandSourceStack>
		): LiteralCommandNode<CommandSourceStack> =
			LiteralArgumentBuilder.literal<CommandSourceStack>(alias)
				.requires(destination.requirement)
				.forward(destination.redirect, destination.redirectModifier, destination.isFork)
				.executes(destination.command)
				.apply { destination.children.forEach { then(it) } }
				.build()

		fun <T : ArgumentBuilder<CommandSourceStack, T>> T.thenLiteral(
			name: String,
			block: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit
		): T = this.then(Commands.literal(name).apply(block))

		fun <T : ArgumentBuilder<CommandSourceStack, T>, ARG> T.thenArgument(
			argumentName: String,
			argument: ArgumentType<ARG>,
			block: RequiredArgumentBuilder<CommandSourceStack, ARG>.() -> Unit
		): T = this.then(Commands.argument(argumentName, argument).apply(block))

		fun <T : ArgumentBuilder<CommandSourceStack, T>> T.thenCommand(command: Command) {
			val node = command.builder.build()
			this.then(node)
			command.aliases.forEach { this.then(buildRedirect(it, node)) }
		}

		fun <T : ArgumentBuilder<CommandSourceStack, T>> T.requiresPermission(permissionLevel: PermissionLevel): T =
			this.requires(Permissions.require(COMMAND_PERM, permissionLevel.value))

		fun <T : ArgumentBuilder<CommandSourceStack, T>> T.requiresPermission(
			permission: String,
			permissionLevel: PermissionLevel
		): T = this.requires(Permissions.require("$COMMAND_PERM.$permission", permissionLevel.value))

		fun CommandContext<CommandSourceStack>.getPlayer(uuid: UUID): Player? =
			this.source.server.playerList.getPlayer(uuid)
	}

	protected val aliases: MutableList<String> = mutableListOf()

	val builder: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(name).apply(builderBlock)

	protected fun alias(alias: String) {
		this.aliases.add(alias)
	}

	@Suppress("SameParameterValue")
	protected fun aliases(vararg aliases: String): Unit = aliases.forEach { this.aliases.add(it) }
}
