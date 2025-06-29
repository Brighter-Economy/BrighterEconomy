package brightspark.brightereconomy.network

import brightspark.brightereconomy.BrighterEconomy
import net.minecraft.client.texture.NativeImage
import net.minecraft.util.Util

object ServerPacketHandler {
	fun onItemTexturePacket(packet: ItemTexturePacket) {
		Util.getIoWorkerExecutor().execute {
			packet.textures.asSequence()
				.map { it.itemId to NativeImage.read(it.bytes) }
				.forEach { (itemName, itemImage) ->
					val imagePath = BrighterEconomy.SERVER_RESOURCES_DIR_PATH.resolve("${itemName}.png")
					itemImage.writeTo(imagePath)
					itemImage.close()
					BrighterEconomy.LOG.atInfo().setMessage("Saved item image {}").addArgument(imagePath).log()
				}
		}
	}
}
