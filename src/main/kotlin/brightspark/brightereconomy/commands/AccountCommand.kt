package brightspark.brightereconomy.commands

import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType.Companion.playerAccountArg
import brightspark.brightereconomy.commands.argtype.PlayerProfileAndAccount
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.util.Util
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

object AccountCommand : Command("account", {
	requiresPermission("account", PermissionLevel.ALL)

	executes { ctx -> AccountCommand.account(ctx) }

	thenArgument("player", playerAccountArg()) {
		requiresPermission("account.player", PermissionLevel.OP)

		executes { ctx ->
			val playerAccount = PlayerAccountArgumentType.get(ctx, "player")
			AccountCommand.account(ctx, playerAccount)
		}
	}
}) {
	private const val KEY_TITLE = "text.brightereconomy.command.account.title"
	private const val KEY_BALANCE = "text.brightereconomy.command.account.details.balance"
	private const val KEY_LOCKED = "text.brightereconomy.command.account.details.locked"
	private const val KEY_LIMIT = "text.brightereconomy.command.account.details.limit"

	private fun account(ctx: CommandContext<CommandSourceStack>, player: PlayerProfileAndAccount? = null): Int {
		fun errorNonPlayer(): Int {
			ctx.source.sendFailure(Component.nullToEmpty("Cannot get account of non-player"))
			return 0
		}

		val name = player?.profile?.name
			?: ctx.source.player?.gameProfile?.name
			?: run { return errorNonPlayer() }
		val account = player?.account
			?: ctx.source.player?.let { EconomyService.getAccount(it.uuid) }
			?: run { return errorNonPlayer() }

		val limit = account.remainingTransferLimit
		val detailPrefix = Util.text("\n| ", Util.COLOUR_PRIMARY_BASE)
		val message = Util.textLang(KEY_TITLE, Util.COLOUR_PRIMARY_BASE, name)
			.append(detailPrefix).append(
				Util.textLang(
					KEY_BALANCE,
					Util.COLOUR_PRIMARY_BASE,
					Util.text(Util.formatMoney(account.money), Util.COLOUR_PRIMARY_ARG)
				)
			)
			.append(detailPrefix).append(
				Util.textLang(
					KEY_LOCKED,
					Util.COLOUR_PRIMARY_BASE,
					Util.text(account.locked, Util.COLOUR_PRIMARY_ARG)
				)
			)
		limit?.let {
			message.append(detailPrefix).append(
				Util.textLang(
					KEY_LIMIT,
					Util.COLOUR_PRIMARY_BASE,
					Util.text(Util.formatMoney(it.toLong()), Util.COLOUR_PRIMARY_ARG)
				)
			)
		}
		ctx.source.sendSystemMessage(message)
		return 1
	}
}
