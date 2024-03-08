package brightspark.brightereconomy

import brightspark.brightereconomy.screen.ShopScreen
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screen.ingame.HandledScreens

@Suppress("unused")
object BrighterEconomyClient : ClientModInitializer {
	override fun onInitializeClient() {
		HandledScreens.register(BrighterEconomy.SHOP_SCREEN_HANDLER, ::ShopScreen)
	}
}
