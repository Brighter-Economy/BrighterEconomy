package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.util.Util
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object SendCommand : Command("send", {
	requiresPermission("send", 0)

	thenArgument("player", playerProfileArg()) {
		thenArgument("amount", longArg(0)) {
			executes { ctx -> SendCommand.send(ctx) }
		}
	}
}) {
	private fun send(ctx: CommandContext<ServerCommandSource>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		val amount = LongArgumentType.getLong(ctx, "amount")
		val result = ctx.getEconomyState().exchange(ctx.source.player?.uuid, player.id, amount, ctx.source.name)
		if (result == TransactionExchangeResult.SUCCESS) {
			ctx.source.sendMessage(Text.of("Sent ${Util.formatMoney(amount)} to ${player.name}"))
			return 1
		} else {
			ctx.source.sendMessage(
				Text.literal("Failed to send ${Util.formatMoney(amount)} to ${player.name} due to ")
					.append(result.text)
			)
			return 0
		}
	}
}
