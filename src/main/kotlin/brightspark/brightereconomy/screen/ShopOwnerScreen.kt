package brightspark.brightereconomy.screen

import brightspark.brightereconomy.util.Util
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ShopOwnerScreen(handler: ShopOwnerScreenHandler, playerInv: PlayerInventory, title: Text) :
	ShopScreen<ShopOwnerScreenHandler>(handler, playerInv, Text.empty()) {

	private var costText: String = "0"

	override fun close() {
		handler.sendData(costText.toInt())
		super.close()
	}

	override fun topHalf(parent: FlowLayout): Unit =
		parent.grid(rows = 2, columns = 2, horizontalSizing = Sizing.fixed(18 * 9), verticalSizing = Sizing.fixed(40)) {
			gridChild(0, 0,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					label(Text.of("Item For Sale:"))
				}
			)
			gridChild(1, 0,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					button(Text.of(" - "), { handler.getShopStack().decrement(1) }) {
						sizing(Sizing.fixed(20))
					}
					texture(Util.SLOT_TEXTURE, 18, 18) {
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

					label(Text.of("Cost:"))
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
		}

	private fun String.isNumber(): Boolean = this.length <= 9 && this.all { it.isDigit() }
}
