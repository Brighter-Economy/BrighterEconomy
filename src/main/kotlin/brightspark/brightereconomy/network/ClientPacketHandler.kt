package brightspark.brightereconomy.network

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.screen.ItemImageGeneratorScreen
import io.wispforest.owo.network.ClientAccess
import net.minecraft.client.resource.language.I18n
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import java.util.stream.Collectors

object ClientPacketHandler {
	fun onClientDataRequestPacket(access: ClientAccess) {
		val enchantmentNames =
			access.runtime().world!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).streamKeys()
				.collect(Collectors.toMap({ it.value.toString() }, { enchantmentTranslatedName(it.value) }))
		BrighterEconomy.NETWORK.clientHandle().send(EnchantmentNamesPacket(enchantmentNames))
		BrighterEconomy.LOG.info("Sent ${enchantmentNames.size} enchantment names to server")

		access.runtime().run { submit { setScreen(ItemImageGeneratorScreen()) } }
	}

	private fun enchantmentTranslatedName(id: Identifier): String =
		I18n.translate(Util.createTranslationKey("enchantment", id))
}
