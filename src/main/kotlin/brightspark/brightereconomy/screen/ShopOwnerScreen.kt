package brightspark.brightereconomy.screen

import brightspark.brightereconomy.util.Util
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component

class ShopOwnerScreen(handler: ShopOwnerScreenHandler, playerInv: Inventory, title: Component) :
	ShopScreen<ShopOwnerScreenHandler>(handler, playerInv, Component.empty()) {

	private var costText: String = "0"

	override fun onClose() {
		menu.sendData(costText.toInt())
		super.onClose()
	}

	override fun topHalf(parent: FlowLayout): Unit =
		parent.grid(rows = 2, columns = 2, horizontalSizing = Sizing.fixed(18 * 9), verticalSizing = Sizing.fixed(40)) {
			gridChild(0, 0,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					label(Component.nullToEmpty("Item For Sale:"))
				}
			)
			gridChild(1, 0,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					button(Component.nullToEmpty(" - "), { menu.getShopStack().shrink(1) }) {
						sizing(Sizing.fixed(20))
					}
					texture(Util.SLOT_TEXTURE, 18, 18) {
						margins(Insets.horizontal(5))
					}
					button(Component.nullToEmpty(" + "), { menu.getShopStack().grow(1) }) {
						sizing(Sizing.fixed(20))
					}
				}
			)

			gridChild(0, 1,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					label(Component.nullToEmpty("Cost:"))
				}
			)
			gridChild(1, 1,
				horizontalFlowComponent(horizontalSizing = Sizing.fill(50), verticalSizing = Sizing.fill(50)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					textBox(Sizing.fixed(70)) {
						verticalSizing(Sizing.fixed(18))

						text(menu.cost.get().toString())
						setFilter { text -> text.isEmpty() || text.isNumber() }
						onChanged().subscribe {
							costText = it.ifEmpty { "0" }
						}
						menu.cost.observe {
							setValue(it.toString())
						}
					}
				}
			)
		}

	private fun String.isNumber(): Boolean = this.length <= 9 && this.all { it.isDigit() }
}
