package brightspark.brightereconomy.commands

import brightspark.brightereconomy.Util
import net.minecraft.text.Text

object BalanceCommand : Command("balance", {
	executes { ctx ->
		ctx.source.player?.let {
			val money = ctx.getEconomyState().getMoney(it.uuid)
			ctx.source.sendMessage(Text.of("Balance: ${Util.formatMoney(money)}"))
			1
		} ?: run {
			ctx.source.sendError(Text.of("Cannot get balance of non-player"))
			0
		}
	}
}) {
	init {
		aliases("bal", "money")
	}
}
