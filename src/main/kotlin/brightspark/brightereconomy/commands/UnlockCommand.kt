package brightspark.brightereconomy.commands

import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object UnlockCommand : Command("unlock", {
	requiresPermission("unlock", 2)

	thenArgument("player", GameProfileArgumentType.gameProfile()) {
		executes { ctx -> UnlockCommand.unlockAccount(ctx) }
	}
}) {
	private fun unlockAccount(ctx: CommandContext<ServerCommandSource>): Int {
		val player = getPlayer(ctx) ?: return 0
		ctx.getEconomyState().unlockAccount(player.id)
		ctx.source.sendMessage(Text.of("Unlocked ${player.name}'s account"))
		return 1
	}
}
