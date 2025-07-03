package brightspark.brightereconomy.commands

import brightspark.brightereconomy.BrighterEconomy
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource

object BaseCommand : Command(BrighterEconomy.MOD_ID, {
	requiresPermission(PermissionLevel.ALL)

	thenCommand(BalanceCommand)
	thenCommand(LockCommand)
	thenCommand(UnlockCommand)
	thenCommand(SendCommand)
	thenCommand(RequestCommand)
	thenCommand(SendClientDataCommand)
	thenCommand(AccountCommand)
}) {
	init {
		BrighterEconomy.CONFIG.commandAliases().asSequence()
			.filter { it.isNotBlank() }
			.forEach { alias(it) }
	}

	fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
		val command = builder.build()
		dispatcher.root.apply {
			addChild(command)
			aliases.forEach { addChild(buildRedirect(it, command)) }
		}
	}
}
