package brightspark.brightereconomy.network

import brightspark.brightereconomy.screen.ItemImageGeneratorScreen
import io.wispforest.owo.network.ClientAccess

object ClientPacketHandler {
	fun onClientDataRequestPacket(access: ClientAccess) {
		access.runtime().run { submit { setScreen(ItemImageGeneratorScreen()) } }
	}
}
