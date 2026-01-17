package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.network.ItemDataPacket
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Screenshot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import kotlin.math.ceil

// This method of image generation was inspired by https://github.com/CyclopsMC/IconExporter
class ItemImageGeneratorScreen : Screen(Component.literal("")) {
	companion object {
		private const val BG_COLOUR: Int = (255 shl 24) or (254 shl 16) or (255 shl 8) or 255
		private const val BG_COLOUR_2: Int = (255 shl 24) or (255 shl 16) or (255 shl 8) or 254
		private const val IMAGE_SIZE: Int = 64
		private const val CHUNKING_SIZE: Int = 50
	}

	private val itemsLeft = BuiltInRegistries.ITEM.entrySet().mapTo(mutableListOf()) { (key, value) -> key.location() to value }
	private val totalItems = itemsLeft.size
	private var doneItems = 0
	private val processedItems = mutableListOf<ItemDataPacket.ItemData>()

	override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)

		if (itemsLeft.isEmpty()) {
			onClose()
		} else {
			val itemPair = itemsLeft.removeFirst()
			val item = itemPair.second
			val image = createItemImage(context, item)
			processedItems += ItemDataPacket.ItemData(
				itemPair.first.toString(),
				I18n.get(item.descriptionId),
				image.asByteArray()
			)
			doneItems += 1
			minecraft!!.player?.displayClientMessage(Component.literal("Working... $doneItems / $totalItems"), true)
		}

		if (processedItems.size >= CHUNKING_SIZE)
			sendToServer()
	}

	override fun renderBackground(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = Unit

	override fun onClose() {
		sendToServer()
		if (itemsLeft.isNotEmpty())
			minecraft!!.player?.sendSystemMessage(Component.literal("Cancelled early at $doneItems / $totalItems"))
		else
			minecraft!!.player?.sendSystemMessage(Component.literal("Finished"))
		super.onClose()
	}

	private fun createItemImage(context: GuiGraphics, item: Item): NativeImage {
		val scaledSize = IMAGE_SIZE / minecraft!!.window.guiScale
		val scaledSizeInt = ceil(scaledSize).toInt()
		context.apply {
			fill(0, 0, scaledSizeInt, scaledSizeInt, BG_COLOUR)
			push()
			val scale = scaledSize.toFloat() / 16.toFloat()
			scale(scale, scale, 1.toFloat())
			renderItem(ItemStack(item), 0, 0)
			pop()
		}

		val image = Screenshot.takeScreenshot(minecraft!!.mainRenderTarget)
		val itemImage = NativeImage(IMAGE_SIZE, IMAGE_SIZE, false)
		image.copyRect(itemImage, 0, 0, 0, 0, IMAGE_SIZE, IMAGE_SIZE, false, false)
		image.close()
		removeBg(itemImage)
		return itemImage
	}

	private fun removeBg(image: NativeImage) {
		(0 until image.width).forEach { x ->
			(0 until image.height).forEach { y ->
				if (image.getPixelRGBA(x, y) == BG_COLOUR_2) {
					image.setPixelRGBA(x, y, 0)
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
