package brightspark.brightereconomy.network

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.ItemLocalisedNameStorage
import net.minecraft.client.texture.NativeImage
import net.minecraft.util.Identifier
import net.minecraft.util.Util

object ServerPacketHandler {
	fun onItemDataPacket(packet: ItemDataPacket): Unit = Util.getIoWorkerExecutor().execute {
		packet.data.forEach {
			val itemId = Identifier(it.itemId)
			val imagePath = BrighterEconomy.SERVER_RESOURCES_DIR_PATH
				.resolve("${itemId.namespace}_${itemId.path}.png")
			NativeImage.read(it.imageBytes).run {
				writeTo(imagePath)
				close()
			}

			ItemLocalisedNameStorage.getStorage().setName(itemId, it.localisedName)

			BrighterEconomy.LOG.atInfo()
				.setMessage("Saved item {} image {} and localised name '{}'")
				.addArgument(itemId).addArgument(imagePath).addArgument(it.localisedName)
				.log()
		}
	}
}
