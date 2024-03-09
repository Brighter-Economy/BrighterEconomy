package brightspark.brightereconomy.commands

import brightspark.brightereconomy.Util
import brightspark.brightereconomy.economy.TransactionExchangeResult
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object BalanceCommand : Command("balance", {
	executes { ctx -> BalanceCommand.balance(ctx) }

	thenArgument("player", GameProfileArgumentType.gameProfile()) {
		executes { ctx ->
			val player = BalanceCommand.getPlayer(ctx) ?: return@executes 0
			BalanceCommand.balance(ctx, player)
		}

		thenLiteral("add") {
			thenArgument("amount", LongArgumentType.longArg(0)) {
				executes { ctx -> BalanceCommand.modifyBalance(ctx, true) }
			}
		}

		thenLiteral("remove") {
			thenArgument("amount", LongArgumentType.longArg(0)) {
				executes { ctx -> BalanceCommand.modifyBalance(ctx, false) }
			}
		}

		thenLiteral("set") {
			thenArgument("amount", LongArgumentType.longArg(0)) {
				executes { ctx -> BalanceCommand.setBalance(ctx) }
			}
		}
	}
}) {
	init {
		aliases("bal", "money")
	}

	private fun getPlayer(ctx: CommandContext<ServerCommandSource>): GameProfile? =
		GameProfileArgumentType.getProfileArgument(ctx, "player").singleOrNull() ?: run {
			ctx.source.sendError(Text.of("0 or many players found with that name."))
			null
		}

	private fun balance(ctx: CommandContext<ServerCommandSource>, player: GameProfile? = null): Int {
		val targetIsSelf = player?.id?.equals(ctx.source.player?.id) ?: true
		return ctx.source.player?.let {
			val money = ctx.getEconomyState().getMoney(it.uuid)
			if (targetIsSelf)
				ctx.source.sendMessage(Text.of("Balance: ${Util.formatMoney(money)}"))
			else
				ctx.source.sendMessage(Text.of("${player?.name}'s Balance: ${Util.formatMoney(money)}"))
			1
		} ?: run {
			ctx.source.sendError(Text.of("Cannot get balance of non-player"))
			0
		}
	}

	private fun modifyBalance(ctx: CommandContext<ServerCommandSource>, add: Boolean): Int {
		val player = getPlayer(ctx) ?: return 0
		val amount = LongArgumentType.getLong(ctx, "amount")
		val result = ctx.getEconomyState().exchange(
			if (add) null else player.id,
			if (add) player.id else null,
			amount,
			ctx.source.name
		)

		return if (result == TransactionExchangeResult.SUCCESS) {
			if (add)
				ctx.source.sendMessage(Text.of("Added $amount to ${player.name}"))
			else
				ctx.source.sendMessage(Text.of("Removed $amount from ${player.name}"))
			1
		} else {
			if (add)
				ctx.source.sendMessage(Text.of("Failed to add $amount to ${player.name} due to $result"))
			else
				ctx.source.sendMessage(Text.of("Failed to remove $amount from ${player.name} due to $result"))
			0
		}
	}

	private fun setBalance(ctx: CommandContext<ServerCommandSource>): Int {
		val player = GameProfileArgumentType.getProfileArgument(ctx, "player").singleOrNull() ?: run {
			ctx.source.sendError(Text.of("0 or many players found with that name."))
			return 0
		}
		val amount = LongArgumentType.getLong(ctx, "amount")
		ctx.getEconomyState().setMoney(player.id, amount, ctx.source.name)
		ctx.source.sendMessage(Text.of("Set ${player.name} balance to $amount"))
		return 1
	}
}
