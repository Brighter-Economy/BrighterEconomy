package brightspark.brightereconomy.commands

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType.Companion.playerProfileArg
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.util.Util
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.ClickEvent.Action.RUN_COMMAND
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object RequestCommand : Command("request", {
	requiresPermission("request", PermissionLevel.ALL)

	thenArgument("player", playerProfileArg()) {
		thenArgument("amount", longArg(0)) {
			executes { ctx -> RequestCommand.request(ctx) }
		}
	}
}) {
	private fun request(ctx: CommandContext<ServerCommandSource>): Int {
		val player = PlayerProfileArgumentType.get(ctx, "player")
		val playerEntity = ctx.getPlayer(player.id) ?: run {
			ctx.source.sendMessage(Text.of("${player.name} isn't online"))
			return 0
		}
		val amount = LongArgumentType.getLong(ctx, "amount")
		val formattedAmount = Util.formatMoney(amount)

		val result = EconomyService.simulateExchange(player.id, ctx.source.player!!.uuid, amount)
		if (result == TransactionExchangeResult.SUCCESS) {
			val acceptCommand = "/${BrighterEconomy.MOD_ID} send ${ctx.source.name} $amount"
			playerEntity.sendMessage(
				Text.literal("${ctx.source.name} has requested $formattedAmount from you - ")
					.append(Text.literal("[Accept]").styled {
						it.withFormatting(Formatting.GREEN)
							.withClickEvent(ClickEvent(RUN_COMMAND, acceptCommand))
					})
			)
			ctx.source.sendMessage(Text.of("Requested $formattedAmount from ${player.name}"))
			return 1
		} else {
			ctx.source.sendMessage(
				Text.literal("Failed to request $formattedAmount from ${player.name} due to ").append(result.text)
			)
			return 0
		}
	}
}
