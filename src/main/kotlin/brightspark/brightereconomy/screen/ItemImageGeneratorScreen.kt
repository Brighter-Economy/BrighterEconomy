package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.network.ItemDataPacket
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import kotlin.math.ceil

// This method of image generation was inspired by https://github.com/CyclopsMC/IconExporter
class ItemImageGeneratorScreen : Screen(Text.literal("")) {
	companion object {
		private const val BG_COLOUR: Int = (255 shl 24) or (254 shl 16) or (255 shl 8) or 255
		private const val BG_COLOUR_2: Int = (255 shl 24) or (255 shl 16) or (255 shl 8) or 254
		private const val IMAGE_SIZE: Int = 64
		private const val CHUNKING_SIZE: Int = 50
	}

	private val itemsLeft = Registries.ITEM.entrySet.mapTo(mutableListOf()) { (key, value) -> key.value to value }
	private val totalItems = itemsLeft.size
	private var doneItems = 0
	private val processedItems = mutableListOf<ItemDataPacket.ItemData>()

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)

		if (itemsLeft.isEmpty()) {
			close()
		} else {
			val itemPair = itemsLeft.removeFirst()
			val item = itemPair.second
			val image = createItemImage(context, item)
			processedItems += ItemDataPacket.ItemData(
				itemPair.first.toString(),
				I18n.translate(item.translationKey),
				image.bytes
			)
			doneItems += 1
			client!!.player?.sendMessage(Text.literal("Working... $doneItems / $totalItems"), true)
		}

		if (processedItems.size >= CHUNKING_SIZE)
			sendToServer()
	}

	override fun renderBackground(context: DrawContext) = Unit

	override fun close() {
		sendToServer()
		if (itemsLeft.isNotEmpty())
			client!!.player?.sendMessage(Text.literal("Cancelled early at $doneItems / $totalItems"))
		else
			client!!.player?.sendMessage(Text.literal("Finished"))
		super.close()
	}

	private fun createItemImage(context: DrawContext, item: Item): NativeImage {
		val scaledSize = IMAGE_SIZE / client!!.window.scaleFactor
		val scaledSizeInt = ceil(scaledSize).toInt()
		@Suppress("UnstableApiUsage")
		context.apply {
			fill(0, 0, scaledSizeInt, scaledSizeInt, BG_COLOUR)
			push()
			val scale = scaledSize.toFloat() / 16.toFloat()
			scale(scale, scale, 1.toFloat())
			drawItem(ItemStack(item), 0, 0)
			pop()
		}

		val image = ScreenshotRecorder.takeScreenshot(client!!.framebuffer)
		val itemImage = NativeImage(IMAGE_SIZE, IMAGE_SIZE, false)
		image.copyRect(itemImage, 0, 0, 0, 0, IMAGE_SIZE, IMAGE_SIZE, false, false)
		image.close()
		removeBg(itemImage)
		return itemImage
	}

	private fun removeBg(image: NativeImage) {
		(0 until image.width).forEach { x ->
			(0 until image.height).forEach { y ->
				if (image.getColor(x, y) == BG_COLOUR_2) {
					image.setColor(x, y, 0)
				}
			}
		}
	}

	private fun sendToServer() {
		if (processedItems.isEmpty())
			return

		BrighterEconomy.NETWORK.clientHandle().send(ItemDataPacket(processedItems.toList()))
		BrighterEconomy.LOG.info("Sent ${processedItems.size} item data to server")
		processedItems.clear()
	}
}
