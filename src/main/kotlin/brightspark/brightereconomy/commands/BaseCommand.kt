package brightspark.brightereconomy.commands

import brightspark.brightereconomy.BrighterEconomy
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource

object BaseCommand : Command(BrighterEconomy.MOD_ID, {
	requiresPermission(0)

	thenCommand(BalanceCommand)
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
