package brightspark.brightereconomy.util

import brightspark.brightereconomy.BrighterEconomy
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
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

	private fun whiteText(): MutableText = Text.empty().styled { it.withFormatting(Formatting.WHITE) }

	private fun whiteText(text: String): Text = Text.literal(text).styled { it.withFormatting(Formatting.WHITE) }

	fun messageText(langKey: String, vararg args: Any): Text =
		Text.translatable(
			langKey,
			*args.map { if (it is Text) whiteText().append(it) else whiteText(it.toString()) }.toTypedArray()
		).styled { it.withFormatting(Formatting.GRAY, Formatting.ITALIC) }
}
