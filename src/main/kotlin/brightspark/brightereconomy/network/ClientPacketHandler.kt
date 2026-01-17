package brightspark.brightereconomy.network

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.screen.ItemImageGeneratorScreen
import io.wispforest.owo.network.ClientAccess
import net.minecraft.client.resources.language.I18n
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.Util
import java.util.stream.Collectors

object ClientPacketHandler {
	fun onClientDataRequestPacket(access: ClientAccess) {
		val enchantmentNames =
			access.runtime().level!!.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElementIds()
				.collect(Collectors.toMap({ it.location().toString() }, { enchantmentTranslatedName(it.location()) }))
		BrighterEconomy.NETWORK.clientHandle().send(EnchantmentNamesPacket(enchantmentNames))
		BrighterEconomy.LOG.info("Sent ${enchantmentNames.size} enchantment names to server")

		access.runtime().run { submit { setScreen(ItemImageGeneratorScreen()) } }
	}

	private fun enchantmentTranslatedName(id: ResourceLocation): String =
		I18n.get(Util.makeDescriptionId("enchantment", id))
}
