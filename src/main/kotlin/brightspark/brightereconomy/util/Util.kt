package brightspark.brightereconomy.util

import brightspark.brightereconomy.BrighterEconomy
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.minecraft.core.component.DataComponentMap
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import java.text.NumberFormat
import java.util.*

object Util {
	val SLOT_TEXTURE = BrighterEconomy.id("textures/gui/slot.png")
	val COLOUR_PRIMARY_BASE = ChatFormatting.AQUA
	val COLOUR_PRIMARY_ARG = ChatFormatting.GREEN
	val COLOUR_SECONDARY_BASE = ChatFormatting.DARK_AQUA
	val COLOUR_SECONDARY_ARG = ChatFormatting.DARK_GREEN

	val GSON = Gson()

	private val FORMAT_MONEY = NumberFormat.getNumberInstance()

	fun formatMoney(money: Long): String = BrighterEconomy.CONFIG.currencySymbol() + FORMAT_MONEY.format(money)

	fun getUsername(uuid: UUID): String? = BrighterEconomy.SERVER
		.flatMap { Optional.ofNullable(it.profileCache) }
		.flatMap { it.get(uuid) }
		.map { it.name }
		.orElse(null)

	fun text(text: Any, colour: ChatFormatting): MutableComponent =
		if (text is Component)
			Component.empty().withStyle { it.withColor(colour) }.append(text)
		else
			Component.literal(text.toString()).withStyle { it.withColor(colour) }

	fun textLang(langKey: String, colour: ChatFormatting, vararg args: Any): MutableComponent =
		Component.translatable(langKey, *args).withStyle { it.withColor(colour) }

	fun messageTextPrimary(langKey: String, vararg args: Any): MutableComponent =
		messageText(COLOUR_PRIMARY_BASE, COLOUR_PRIMARY_ARG, langKey, args)

	fun messageTextSecondary(langKey: String, vararg args: Any): MutableComponent =
		messageText(COLOUR_SECONDARY_BASE, COLOUR_SECONDARY_ARG, langKey, args)

	private fun messageText(
		baseColour: ChatFormatting,
		argColour: ChatFormatting,
		langKey: String,
		args: Array<out Any>
	): MutableComponent {
		val textArgs = args.map { text(it, argColour) }.toTypedArray()
		return Component.translatable(langKey, *textArgs).withStyle { it.withColor(baseColour) }
	}

	fun componentsToJsonString(components: DataComponentMap): Optional<String> =
		DataComponentMap.CODEC.encodeStart(JsonOps.COMPRESSED, components)
			.resultOrPartial { BrighterEconomy.LOG.error("Error serialising components: $it") }
			.map { GSON.toJson(it) }

	fun jsonStringToComponents(jsonString: String): Optional<DataComponentMap> =
		GSON.fromJson(jsonString, JsonObject::class.java)
			.let { DataComponentMap.CODEC.decode(JsonOps.COMPRESSED, it) }
			.resultOrPartial { BrighterEconomy.LOG.error("Error deserialising components: $it") }
			.map { it.first }
}
