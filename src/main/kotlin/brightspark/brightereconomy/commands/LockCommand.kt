package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.EconomyService
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object LockCommand : Command("lock", {
	requiresPermission("lock", 2)

	thenArgument("player", playerProfileArg()) {
		executes { ctx -> LockCommand.lockAccount(ctx) }
	}
}) {
	private fun lockAccount(ctx: CommandContext<ServerCommandSource>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		EconomyService.lockAccount(player.id)
		ctx.source.sendMessage(Text.of("Locked ${player.name}'s account"))
		return 1
	}
}
