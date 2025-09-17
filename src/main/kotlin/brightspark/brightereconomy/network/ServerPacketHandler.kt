package brightspark.brightereconomy.network

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.LocalisedNameStorage
import net.minecraft.client.texture.NativeImage
import net.minecraft.util.Identifier
import net.minecraft.util.Util

object ServerPacketHandler {
	fun onItemDataPacket(packet: ItemDataPacket): Unit = Util.getIoWorkerExecutor().execute {
		LocalisedNameStorage.getStorage().apply {
			packet.data.forEach {
				val itemId = Identifier(it.itemId)
				val imagePath = BrighterEconomy.SERVER_RESOURCES_DIR_PATH
					.resolve("${itemId.namespace}_${itemId.path}.png")
				NativeImage.read(it.imageBytes).run {
					writeTo(imagePath)
					close()
				}

				BrighterEconomy.LOG.atDebug()
					.setMessage("Received item {} image {}")
					.addArgument(itemId).addArgument(imagePath)
					.log()
			}

			setItemNames(
				packet.data.asSequence()
					.map { it.itemId to it.localisedName }
					.onEach {
						BrighterEconomy.LOG.atDebug()
							.setMessage("Received item {} localised name '{}'")
							.addArgument(it.first).addArgument(it.second)
							.log()
					}
			)
		}
		BrighterEconomy.LOG.atInfo().setMessage("Received data for {} items").addArgument(packet.data.size).log()
	}

	fun onLocalisedNamesPacket(packet: EnchantmentNamesPacket) {
		LocalisedNameStorage.getStorage().apply {
			setEnchantmentNames(
				packet.data.asSequence()
					.map { it.key to it.value }
					.onEach { (id, name) ->
						BrighterEconomy.LOG.atDebug()
							.setMessage("Received enchantment {} localised name '{}'")
							.addArgument(id).addArgument(name)
							.log()
					}
			)
		}
		BrighterEconomy.LOG.atInfo().setMessage("Received data for {} enchantments").addArgument(packet.data.size).log()
	}
}
