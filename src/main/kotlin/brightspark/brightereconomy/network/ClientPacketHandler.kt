package brightspark.brightereconomy.network

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.screen.ItemImageGeneratorScreen
import io.wispforest.owo.network.ClientAccess
import net.minecraft.client.resource.language.I18n
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.util.Util

object ClientPacketHandler {
	fun onClientDataRequestPacket(access: ClientAccess) {
		val enchantmentNames = access.runtime().world!!.registryManager.getOrThrow(RegistryKeys.ENCHANTMENT).keys
			.associate { it.value.toString() to enchantmentTranslatedName(it.value) }
		BrighterEconomy.NETWORK.clientHandle().send(EnchantmentNamesPacket(enchantmentNames))
		BrighterEconomy.LOG.info("Sent ${enchantmentNames.size} enchantment names to server")

		access.runtime().run { submit { setScreen(ItemImageGeneratorScreen()) } }
	}

	private fun enchantmentTranslatedName(id: Identifier): String =
		I18n.translate(Util.createTranslationKey("enchantment", id))
}
