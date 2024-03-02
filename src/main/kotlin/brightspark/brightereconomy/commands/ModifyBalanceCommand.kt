package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.ModifyBalanceCommand.modifyBalance
import brightspark.brightereconomy.economy.TransactionExchangeResult
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object ModifyBalanceCommand : Command("modifybalance", {
	requiresPermission("modifybalance")

	thenLiteral("add") {
		thenArgument("player", GameProfileArgumentType.gameProfile()) {
			thenArgument("amount", IntegerArgumentType.integer()) {
				executes { ctx -> modifyBalance(ctx, true) }
			}
		}
	}

	thenLiteral("remove") {
		thenArgument("player", GameProfileArgumentType.gameProfile()) {
			thenArgument("amount", IntegerArgumentType.integer()) {
				executes { ctx -> modifyBalance(ctx, false) }
			}
		}
	}
}) {
	init {
		aliases("modify", "modbal")
	}

	fun modifyBalance(ctx: CommandContext<ServerCommandSource>, add: Boolean): Int {
		val player = GameProfileArgumentType.getProfileArgument(ctx, "player").singleOrNull() ?: run {
			ctx.source.sendError(Text.of("0 or many players found with that name."))
			return 0
		}
		val amount = IntegerArgumentType.getInteger(ctx, "amount")
		val result = ctx.getEconomyState().exchange(
			if (add) null else player.id,
			if (add) player.id else null,
			amount.toLong(),
			ctx.source.name
		)

		return if (result == TransactionExchangeResult.SUCCESS) {
			if (add)
				ctx.source.sendMessage(Text.of("Added $amount to ${player.name}"))
			else
				ctx.source.sendMessage(Text.of("Removed $amount from ${player.name}"))
			amount
		} else {
			if (add)
				ctx.source.sendMessage(Text.of("Failed to add $amount to ${player.name} due to $result"))
			else
				ctx.source.sendMessage(Text.of("Failed to remove $amount from ${player.name} due to $result"))
			0
		}
	}
}
