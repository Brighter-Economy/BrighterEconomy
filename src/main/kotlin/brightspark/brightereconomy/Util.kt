package brightspark.brightereconomy

import java.text.NumberFormat

object Util {
	private val FORMAT_MONEY = NumberFormat.getNumberInstance()

	fun formatMoney(money: Long): String = FORMAT_MONEY.format(money)
}
