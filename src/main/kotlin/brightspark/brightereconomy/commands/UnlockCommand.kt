package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.EconomyService
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

object UnlockCommand : Command("unlock", {
	requiresPermission("unlock", PermissionLevel.OP)

	thenArgument("player", playerProfileArg()) {
		executes { ctx -> UnlockCommand.unlockAccount(ctx) }
	}
}) {
	private fun unlockAccount(ctx: CommandContext<CommandSourceStack>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		EconomyService.unlockAccount(player.id)
		ctx.source.sendSystemMessage(Component.nullToEmpty("Unlocked ${player.name}'s account"))
		return 1
	}
}
