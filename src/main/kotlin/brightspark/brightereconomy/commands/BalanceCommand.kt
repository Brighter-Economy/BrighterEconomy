package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType.Companion.playerAccountArg
import brightspark.brightereconomy.commands.argtype.PlayerProfileAndAccount
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.economy.TransactionExchangeResult
import brightspark.brightereconomy.util.Util
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting

object BalanceCommand : Command("balance", {
	requiresPermission("balance", PermissionLevel.ALL)

	executes { ctx -> BalanceCommand.balance(ctx) }

	thenArgument("player", playerAccountArg()) {
		requiresPermission("balance.player", PermissionLevel.OP)

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

	private fun balance(ctx: CommandContext<CommandSourceStack>, player: PlayerProfileAndAccount? = null): Int {
		val profile = player?.profile
		val targetIsSelf = profile?.id?.equals(ctx.source.player?.id) ?: true
		return ctx.source.player?.let { playerEntity ->
			val account = EconomyService.getAccount(playerEntity.uuid)
			val money = Util.formatMoney(account.money)
			val textString = "${if (!targetIsSelf) "${profile.name}'s " else ""}Balance: $money"
			var text = Component.literal(textString)
			if (account.locked)
				text = text.append(Component.literal(" [locked]").withStyle { it.withColor(ChatFormatting.RED) })

			ctx.source.sendSystemMessage(text)
			1
		} ?: run {
			ctx.source.sendFailure(Component.nullToEmpty("Cannot get balance of non-player"))
			0
		}
	}

	private fun modifyBalance(ctx: CommandContext<CommandSourceStack>, add: Boolean): Int {
		val player = PlayerAccountArgumentType.get(ctx, "player")
		val playerId = player.profile.id
		val playerName = player.profile.name
		val amount = LongArgumentType.getLong(ctx, "amount")
		val result = EconomyService.modify(playerId, add, amount, ctx.source.textName)

		val amountFormatted = Util.formatMoney(amount)
		if (result == TransactionExchangeResult.SUCCESS) {
			if (add)
				ctx.source.sendSystemMessage(Component.nullToEmpty("Added $amountFormatted to $playerName"))
			else
				ctx.source.sendSystemMessage(Component.nullToEmpty("Removed $amountFormatted from $playerName"))
			return 1
		} else {
			if (add)
				ctx.source.sendSystemMessage(
					Component.literal("Failed to add $amountFormatted to $playerName due to ")
						.append(result.text)
				)
			else
				ctx.source.sendSystemMessage(
					Component.literal("Failed to remove $amountFormatted from $playerName due to $result")
						.append(result.text)
				)
			return 0
		}
	}

	private fun setBalance(ctx: CommandContext<CommandSourceStack>): Int {
		val player = PlayerAccountArgumentType.get(ctx, "player")
		val amount = LongArgumentType.getLong(ctx, "amount")
		EconomyService.set(player.profile.id, amount, ctx.source.textName)
		ctx.source.sendSystemMessage(Component.nullToEmpty("Set ${Util.formatMoney(amount)} to ${player.profile.name}"))
		return 1
	}
}
