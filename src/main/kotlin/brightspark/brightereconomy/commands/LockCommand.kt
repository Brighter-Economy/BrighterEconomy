package brightspark.brightereconomy.commands

import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object LockCommand : Command("lock", {
	requiresPermission("lock", 2)

	thenArgument("player", GameProfileArgumentType.gameProfile()) {
		executes { ctx -> LockCommand.lockAccount(ctx) }
	}
}) {
	private fun lockAccount(ctx: CommandContext<ServerCommandSource>): Int {
		val player = getPlayer(ctx) ?: return 0
		ctx.getEconomyState().lockAccount(player.id)
		ctx.source.sendMessage(Text.of("Locked ${player.name}'s account"))
		return 1
	}
}
