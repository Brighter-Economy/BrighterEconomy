package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ShopScreen(handler: ShopScreenHandler, playerInv: PlayerInventory, title: Text) :
	BaseOwoHandledScreen<FlowLayout, ShopScreenHandler>(handler, playerInv, Text.empty()) {
	companion object {
		private val SLOT_TEXTURE = Identifier(BrighterEconomy.MOD_ID, "textures/gui/slot.png")
	}

	override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

	override fun build(root: FlowLayout) {
		root.apply {
			surface(Surface.VANILLA_TRANSLUCENT)
			horizontalAlignment(HorizontalAlignment.CENTER)
			verticalAlignment(VerticalAlignment.CENTER)

			verticalFlow {
				surface(Surface.PANEL)
				padding(Insets.of(10))

				topHalf()
				bottomHalf()
			}
		}
	}

	private fun FlowLayout.topHalf(): Unit =
		horizontalFlow(horizontalSizing = Sizing.fixed(18 * 9), verticalSizing = Sizing.fixed(40)) {
			verticalFlow(verticalSizing = Sizing.fill(100)) {
				padding(Insets.horizontal(5))
				horizontalAlignment(HorizontalAlignment.CENTER)
				verticalAlignment(VerticalAlignment.CENTER)

				texture(SLOT_TEXTURE, 18, 18)
				horizontalFlow {
					verticalAlignment(VerticalAlignment.CENTER)

					button(Text.of("-"), { handler.getShopStack().decrement(1) })
					textBox(Sizing.fixed(20)) {
						setTextPredicate { text ->
							text.isEmpty() || (text.length <= 2 && text.isNumber(max = handler.getShopStack().maxCount))
						}
					}
					button(Text.of("+"), { handler.getShopStack().increment(1) })
				}
			}

			verticalFlow(horizontalSizing = Sizing.fixed(102), verticalSizing = Sizing.fill(100)) {
				horizontalAlignment(HorizontalAlignment.CENTER)
				verticalAlignment(VerticalAlignment.CENTER)

				label(Text.of("Cost:")) {
					shadow(true)
					margins(Insets.bottom(5))
				}
				textBox(Sizing.fixed(100)) {
					setTextPredicate { text -> text.isEmpty() || (text.length <= 9 && text.all { it.isDigit() }) }
					onChanged().subscribe { handler.cost = it.toInt() }
				}
			}
		}

	private fun FlowLayout.bottomHalf(): Unit = verticalFlow {
		margins(Insets.top(10))

		for (y in 0..2) {
			horizontalFlow {
				for (x in 0..8) texture(SLOT_TEXTURE, 18, 18)
			}
		}
		horizontalFlow {
			margins(Insets.top(4))
			for (x in 0..8) texture(SLOT_TEXTURE, 18, 18)
		}
	}

	private fun String.isNumber(max: Int): Boolean = this.all { it.isDigit() } && this.toInt() in 0..max
}
