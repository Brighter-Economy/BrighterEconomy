package brightspark.brightereconomy.screen

import brightspark.brightereconomy.util.Util
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text

abstract class ShopScreen<H : ScreenHandler>(handler: H, playerInv: PlayerInventory, title: Text) :
	BaseOwoHandledScreen<FlowLayout, H>(handler, playerInv, title) {
	override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

	override fun build(root: FlowLayout) {
		root.apply {
			surface(Surface.VANILLA_TRANSLUCENT)
			horizontalAlignment(HorizontalAlignment.CENTER)
			verticalAlignment(VerticalAlignment.CENTER)

			verticalFlow {
				surface(Surface.PANEL)
				padding(Insets.of(10))

				topHalf(this)
				bottomHalf()
			}
		}
	}

	protected abstract fun topHalf(parent: FlowLayout)

	private fun FlowLayout.bottomHalf(): Unit = verticalFlow {
		margins(Insets.top(10))

		for (y in 0..2) {
			horizontalFlow {
				for (x in 0..8) texture(Util.SLOT_TEXTURE, 18, 18)
			}
		}
		horizontalFlow {
			margins(Insets.top(4))
			for (x in 0..8) texture(Util.SLOT_TEXTURE, 18, 18)
		}
	}
}
