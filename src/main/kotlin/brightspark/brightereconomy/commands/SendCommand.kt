package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.util.Util
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

object SendCommand : Command("send", {
	requiresPermission("send", PermissionLevel.ALL)

	thenArgument("player", playerProfileArg()) {
		thenArgument("amount", longArg(0)) {
			executes { ctx -> SendCommand.send(ctx) }
		}
	}
}) {
	private fun send(ctx: CommandContext<CommandSourceStack>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		val sourcePlayerId = ctx.source.player?.uuid
		val amount = LongArgumentType.getLong(ctx, "amount")
		val result = EconomyService.transfer(sourcePlayerId, player.id, amount, ctx.source.textName)
		val formattedAmount = Util.formatMoney(amount)
		when (result) {
			TransactionExchangeResult.SUCCESS -> {
				ctx.source.sendSystemMessage(Component.nullToEmpty("Sent $formattedAmount to ${player.name}"))
				ctx.getPlayer(player.id)?.sendSystemMessage(Component.nullToEmpty("${ctx.source.textName} has sent you $formattedAmount"))
				return 1
			}
			TransactionExchangeResult.OVER_DAILY_LIMIT -> {
				val remainingLimit = EconomyService.getRemainingTransferLimit(sourcePlayerId)
					?.let { Util.formatMoney(it.toLong()) }
				ctx.source.sendSystemMessage(
					Component.literal("Failed to send $formattedAmount to ${player.name} due to ")
						.append(result.text)
						.append(Component.literal(" (max $remainingLimit today)"))
				)
				return 0
			}
			else -> {
				ctx.source.sendSystemMessage(
					Component.literal("Failed to send $formattedAmount to ${player.name} due to ")
						.append(result.text)
				)
				return 0
			}
		}
	}
}
