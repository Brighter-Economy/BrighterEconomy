package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.EconomyService
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

object LockCommand : Command("lock", {
	requiresPermission("lock", PermissionLevel.OP)

	thenArgument("player", playerProfileArg()) {
		executes { ctx -> LockCommand.lockAccount(ctx) }
	}
}) {
	private fun lockAccount(ctx: CommandContext<CommandSourceStack>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		EconomyService.lockAccount(player.id)
		ctx.source.sendSystemMessage(Component.nullToEmpty("Locked ${player.name}'s account"))
		return 1
	}
}
