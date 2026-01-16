package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.EconomyService
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object UnlockCommand : Command("unlock", {
	requiresPermission("unlock", PermissionLevel.OP)

	thenArgument("player", playerProfileArg()) {
		executes { ctx -> UnlockCommand.unlockAccount(ctx) }
	}
}) {
	private fun unlockAccount(ctx: CommandContext<ServerCommandSource>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		EconomyService.unlockAccount(player.id, ctx.source.name)
		ctx.source.sendMessage(Text.of("Unlocked ${player.name}'s account"))
		return 1
	}
}
