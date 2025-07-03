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
	val COLOUR_PRIMARY_BASE = Formatting.AQUA
	val COLOUR_PRIMARY_ARG = Formatting.GREEN
	val COLOUR_SECONDARY_BASE = Formatting.DARK_AQUA
	val COLOUR_SECONDARY_ARG = Formatting.DARK_GREEN

	private val FORMAT_MONEY = NumberFormat.getNumberInstance()

	fun formatMoney(money: Long): String = BrighterEconomy.CONFIG.currencySymbol() + FORMAT_MONEY.format(money)

	fun getUsername(uuid: UUID): String? = BrighterEconomy.SERVER
		.flatMap { Optional.ofNullable(it.userCache) }
		.flatMap { it.getByUuid(uuid) }
		.map { it.name }
		.orElse(null)

	fun text(text: Any, colour: Formatting): MutableText =
		if (text is Text)
			Text.empty().styled { it.withColor(colour) }.append(text)
		else
			Text.literal(text.toString()).styled { it.withColor(colour) }

	fun textLang(langKey: String, colour: Formatting, vararg args: Any): MutableText =
		Text.translatable(langKey, *args).styled { it.withColor(colour) }

	fun messageTextPrimary(langKey: String, vararg args: Any): MutableText =
		messageText(COLOUR_PRIMARY_BASE, COLOUR_PRIMARY_ARG, langKey, args)

	fun messageTextSecondary(langKey: String, vararg args: Any): MutableText =
		messageText(COLOUR_SECONDARY_BASE, COLOUR_SECONDARY_ARG, langKey, args)

	private fun messageText(
		baseColour: Formatting,
		argColour: Formatting,
		langKey: String,
		args: Array<out Any>
	): MutableText {
		val textArgs = args.map { text(it, argColour) }.toTypedArray()
		return Text.translatable(langKey, *textArgs).styled { it.withColor(baseColour) }
	}
}
