package brightspark.brightereconomy.util

import brightspark.brightereconomy.BrighterEconomy
import java.text.NumberFormat

object Util {
	private val FORMAT_MONEY = NumberFormat.getNumberInstance()

	fun formatMoney(money: Long): String = BrighterEconomy.CONFIG.currencySymbol() + FORMAT_MONEY.format(money)
}
