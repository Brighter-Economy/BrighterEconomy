package brightspark.brightereconomy.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource

object BaseCommand : Command("brightereconomy", {
	thenCommand(BalanceCommand)
	thenCommand(ModifyBalanceCommand)
}) {
	init {
		aliases("be")
	}

	fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
		val command = builder.build()
		dispatcher.root.apply {
			addChild(command)
			aliases.forEach { addChild(buildRedirect(it, command)) }
		}
	}
}
