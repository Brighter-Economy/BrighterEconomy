package brightspark.brightereconomy.network

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.screen.ItemImageGeneratorScreen
import io.wispforest.owo.network.ClientAccess
import net.minecraft.client.resource.language.I18n
import net.minecraft.registry.Registries

object ClientPacketHandler {
	fun onClientDataRequestPacket(access: ClientAccess) {
		val enchantmentNames = Registries.ENCHANTMENT.entrySet.asSequence()
			.map { it.key.value.toString() to I18n.translate(it.value.translationKey) }
			.toMap()
		BrighterEconomy.NETWORK.clientHandle().send(EnchantmentNamesPacket(enchantmentNames))
		BrighterEconomy.LOG.info("Sent ${enchantmentNames.size} enchantment names to server")

		access.runtime().run { submit { setScreen(ItemImageGeneratorScreen()) } }
	}
}
