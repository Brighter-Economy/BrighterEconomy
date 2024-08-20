package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType.Companion.playerAccountArg
import brightspark.brightereconomy.commands.argtype.PlayerProfileAndAccount
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.util.Util
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object BalanceCommand : Command("balance", {
	requiresPermission("balance", 0)

	executes { ctx -> BalanceCommand.balance(ctx) }

	thenArgument("player", playerAccountArg()) {
		requiresPermission("player", 2)

		executes { ctx ->
			val playerAccount = PlayerAccountArgumentType.get(ctx, "player")
			BalanceCommand.balance(ctx, playerAccount)
		}

		thenLiteral("add") {
			thenArgument("amount", longArg(1)) {
				executes { ctx -> BalanceCommand.modifyBalance(ctx, true) }
			}
		}

		thenLiteral("remove") {
			thenArgument("amount", longArg(1)) {
				executes { ctx -> BalanceCommand.modifyBalance(ctx, false) }
			}
		}

		thenLiteral("set") {
			thenArgument("amount", longArg(0)) {
				executes { ctx -> BalanceCommand.setBalance(ctx) }
			}
		}
	}
}) {
	init {
		aliases("bal", "money")
	}

	private fun balance(ctx: CommandContext<ServerCommandSource>, player: PlayerProfileAndAccount? = null): Int {
		val playerId = player?.profile?.id
		val targetIsSelf = playerId?.equals(ctx.source.player?.id) ?: true
		return ctx.source.player?.let { playerEntity ->
			val account = ctx.getEconomyState().getAccount(playerEntity.uuid)
			val money = Util.formatMoney(account.money)
			val textString = "${if (!targetIsSelf) "${player?.profile?.name}'s " else ""}Balance: $money"
			var text = Text.literal(textString)
			if (account.locked)
				text = text.append(Text.literal(" [locked]").styled { it.withColor(Formatting.RED) })

			ctx.source.sendMessage(text)
			1
		} ?: run {
			ctx.source.sendError(Text.of("Cannot get balance of non-player"))
			0
		}
	}

	private fun modifyBalance(ctx: CommandContext<ServerCommandSource>, add: Boolean): Int {
		val player = PlayerAccountArgumentType.get(ctx, "player")
		val playerId = player.profile.id
		val playerName = player.profile.name
		val amount = LongArgumentType.getLong(ctx, "amount")
		val result = ctx.getEconomyState().exchange(
			if (add) null else playerId,
			if (add) playerId else null,
			amount,
			ctx.source.name
		)

		val amountFormatted = Util.formatMoney(amount)
		if (result == TransactionExchangeResult.SUCCESS) {
			if (add)
				ctx.source.sendMessage(Text.of("Added $amountFormatted to $playerName"))
			else
				ctx.source.sendMessage(Text.of("Removed $amountFormatted from $playerName"))
			return 1
		} else {
			if (add)
				ctx.source.sendMessage(
					Text.literal("Failed to add $amountFormatted to $playerName due to ")
						.append(result.text)
				)
			else
				ctx.source.sendMessage(
					Text.literal("Failed to remove $amountFormatted from $playerName due to $result")
						.append(result.text)
				)
			return 0
		}
	}

	private fun setBalance(ctx: CommandContext<ServerCommandSource>): Int {
		val player = PlayerAccountArgumentType.get(ctx, "player")
		val amount = LongArgumentType.getLong(ctx, "amount")
		ctx.getEconomyState().setMoney(player.profile.id, amount, ctx.source.name)
		ctx.source.sendMessage(Text.of("Sent ${Util.formatMoney(amount)} to ${player.profile.name}"))
		return 1
	}
}
