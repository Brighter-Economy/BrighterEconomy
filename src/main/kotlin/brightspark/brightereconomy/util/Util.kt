package brightspark.brightereconomy.util

import brightspark.brightereconomy.BrighterEconomy
import net.minecraft.util.Identifier
import java.text.NumberFormat
import java.util.*

object Util {
	val SLOT_TEXTURE = Identifier(BrighterEconomy.MOD_ID, "textures/gui/slot.png")

	private val FORMAT_MONEY = NumberFormat.getNumberInstance()

	fun formatMoney(money: Long): String = BrighterEconomy.CONFIG.currencySymbol() + FORMAT_MONEY.format(money)

	fun getUsername(uuid: UUID): String? = BrighterEconomy.SERVER
		.flatMap { Optional.ofNullable(it.userCache) }
		.flatMap { it.getByUuid(uuid) }
		.map { it.name }
		.orElse(null)
}
