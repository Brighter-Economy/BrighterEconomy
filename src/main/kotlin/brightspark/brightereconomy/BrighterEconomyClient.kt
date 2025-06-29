package brightspark.brightereconomy

import brightspark.brightereconomy.network.ClientDataRequestPacket
import brightspark.brightereconomy.network.ClientPacket
import brightspark.brightereconomy.screen.ShopCustomerScreen
import brightspark.brightereconomy.screen.ShopOwnerScreen
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screen.ingame.HandledScreens

@Suppress("unused")
object BrighterEconomyClient : ClientModInitializer {
	override fun onInitializeClient() {
		regClientPacket<ClientDataRequestPacket>()

		HandledScreens.register(BrighterEconomy.SHOP_OWNER_SCREEN_HANDLER, ::ShopOwnerScreen)
		HandledScreens.register(BrighterEconomy.SHOP_CUSTOMER_SCREEN_HANDLER, ::ShopCustomerScreen)
	}

	inline fun <reified T> regClientPacket() where T : Record, T : ClientPacket {
		BrighterEconomy.NETWORK.registerClientbound<T>(T::class.java) { message, access -> message.handle(access) }
	}
}
