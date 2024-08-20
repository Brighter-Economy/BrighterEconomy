package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object UnlockCommand : Command("unlock", {
	requiresPermission("unlock", 2)

	thenArgument("player", playerProfileArg()) {
		executes { ctx -> UnlockCommand.unlockAccount(ctx) }
	}
}) {
	private fun unlockAccount(ctx: CommandContext<ServerCommandSource>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		ctx.getEconomyState().unlockAccount(player.id)
		ctx.source.sendMessage(Text.of("Unlocked ${player.name}'s account"))
		return 1
	}
}
