package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.util.Util
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object SendCommand : Command("send", {
	requiresPermission("send", PermissionLevel.ALL)

	thenArgument("player", playerProfileArg()) {
		thenArgument("amount", longArg(0)) {
			executes { ctx -> SendCommand.send(ctx) }
		}
	}
}) {
	private fun send(ctx: CommandContext<ServerCommandSource>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		val sourcePlayerId = ctx.source.player?.uuid
		val amount = LongArgumentType.getLong(ctx, "amount")
		val result = EconomyService.transfer(sourcePlayerId, player.id, amount, ctx.source.name)
		val formattedAmount = Util.formatMoney(amount)
		when (result) {
			TransactionExchangeResult.SUCCESS -> {
				ctx.source.sendMessage(Text.of("Sent $formattedAmount to ${player.name}"))
				ctx.getPlayer(player.id)?.sendMessage(Text.of("${ctx.source.name} has sent you $formattedAmount"))
				return 1
			}
			TransactionExchangeResult.OVER_DAILY_LIMIT -> {
				val remainingLimit = EconomyService.getRemainingTransferLimit(sourcePlayerId)
					?.let { Util.formatMoney(it.toLong()) }
				ctx.source.sendMessage(
					Text.literal("Failed to send $formattedAmount to ${player.name} due to ")
						.append(result.text)
						.append(Text.literal(" (max $remainingLimit today)"))
				)
				return 0
			}
			else -> {
				ctx.source.sendMessage(
					Text.literal("Failed to send $formattedAmount to ${player.name} due to ")
						.append(result.text)
				)
				return 0
			}
		}
	}
}
