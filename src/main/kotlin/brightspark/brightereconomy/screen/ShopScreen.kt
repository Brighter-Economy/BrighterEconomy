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

	private var costText: String = "0"

	override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

	override fun close() {
		handler.sendData(costText.toInt())
		super.close()
	}

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
		grid(rows = 2, columns = 2, horizontalSizing = Sizing.fixed(18 * 9), verticalSizing = Sizing.fixed(40)) {
			gridChild(0, 0,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					label(Text.of("Item For Sale:")) {
						shadow(true)
//					margins(Insets.bottom(5))
					}
				}
			)
			gridChild(1, 0,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					button(Text.of(" - "), { handler.getShopStack().decrement(1) }) {
						sizing(Sizing.fixed(20))
					}
					texture(SLOT_TEXTURE, 18, 18) {
						margins(Insets.horizontal(5))
					}
					button(Text.of(" + "), { handler.getShopStack().increment(1) }) {
						sizing(Sizing.fixed(20))
					}
				}
			)

			gridChild(0, 1,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					label(Text.of("Cost:")) {
						shadow(true)
//					margins(Insets.bottom(5))
					}
				}
			)
			gridChild(1, 1,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					textBox(Sizing.fixed(70)) {
						verticalSizing(Sizing.fixed(18))

						text(handler.cost.get().toString())
						setTextPredicate { text -> text.isEmpty() || text.isNumber() }
						onChanged().subscribe {
							costText = it.ifEmpty { "0" }
						}
						handler.cost.observe {
							text = it.toString()
						}
					}
				}
			)

//			verticalFlow(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(100)) {
//				padding(Insets.horizontal(5))
//				horizontalAlignment(HorizontalAlignment.CENTER)
//				verticalAlignment(VerticalAlignment.CENTER)
//
//
//			}

//			verticalFlow(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(100)) {
//				horizontalAlignment(HorizontalAlignment.CENTER)
//				verticalAlignment(VerticalAlignment.CENTER)
//
//			}
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

	private fun String.isNumber(): Boolean = this.length <= 9 && this.all { it.isDigit() }
}
