package brightspark.brightereconomy

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun PlayerEntity.sendLiteralOverlayMessage(message: String, format: Formatting? = null): Unit =
	this.sendMessage(Text.literal(message).apply { format?.let { this.styled { it.withFormatting(format) } } }, true)
