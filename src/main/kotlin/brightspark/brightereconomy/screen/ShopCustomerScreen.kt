package brightspark.brightereconomy.screen

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.util.Util
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.math.min

class ShopCustomerScreen(handler: ShopCustomerScreenHandler, playerInv: PlayerInventory, title: Text) :
	ShopScreen<ShopCustomerScreenHandler>(handler, playerInv, Text.empty()) {

	override fun topHalf(parent: FlowLayout): Unit =
		parent.horizontalFlow(horizontalSizing = Sizing.fixed(18 * 9)) {
			verticalAlignment(VerticalAlignment.CENTER)

			verticalFlow(horizontalSizing = Sizing.fixed(80)) {
				verticalAlignment(VerticalAlignment.CENTER)
				horizontalAlignment(HorizontalAlignment.CENTER)

				label(Text.of("For Sale:"))

				horizontalFlow {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					item(handler.forSaleStack) {
						setTooltipFromStack(true)
					}
					label(handler.forSaleStack, { Text.of("x ${it.count}") }) {
						margins(Insets.left(5))
					}
				}

				label(handler.cost, { Text.of("Cost: ${BrighterEconomy.CONFIG.currencySymbol()}$it") })
				label(handler.stock, { Text.of("Stock: $it") })
			}

			verticalFlow(horizontalSizing = Sizing.fixed(82)) {
				verticalAlignment(VerticalAlignment.CENTER)
				horizontalAlignment(HorizontalAlignment.CENTER)

				fun ButtonComponent.updateTooltip(num: Int, account: PlayerAccount) {
					tooltip(
						"Buying: ${num * handler.forSaleStack.get().count}",
						"Cost: ${Util.formatMoney(num * handler.cost.get().toLong())}",
						"Your Balance: ${Util.formatMoney(account.money)}"
					)
				}

				fun FlowLayout.buyButton(text: String, num: Int, block: ButtonComponent.() -> Unit) =
					this.button(Text.of(text), { buy(num) }, block)

				fun FlowLayout.buyButton(num: Int) = buyButton("Buy $num", num) {
					sizing(Sizing.fixed(40), Sizing.fixed(12))
					margins(Insets.of(1))
					fun updateActive(stock: Int, cost: Int, account: PlayerAccount) {
						active = stock >= num && handler.playerCanBuy(num, cost, account)
					}
					updateActive(handler.stock.get(), handler.cost.get(), handler.playerAccount.get())
					updateTooltip(num, handler.playerAccount.get())

					handler.forSaleStack.observe { updateTooltip(num, handler.playerAccount.get()) }
					handler.stock.observe { updateActive(it, handler.cost.get(), handler.playerAccount.get()) }
					handler.cost.observe {
						updateActive(handler.stock.get(), it, handler.playerAccount.get())
						updateTooltip(num, handler.playerAccount.get())
					}
					handler.playerAccount.observe {
						updateActive(handler.stock.get(), handler.cost.get(), it)
						updateTooltip(num, it)
					}
				}

				horizontalFlow(horizontalSizing = Sizing.fill(100)) {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					verticalFlow(horizontalSizing = Sizing.fill(50)) {
						verticalAlignment(VerticalAlignment.CENTER)
						horizontalAlignment(HorizontalAlignment.RIGHT)

						buyButton(1)
						buyButton(10)
						buyButton(32)
					}

					verticalFlow(horizontalSizing = Sizing.fill(50)) {
						verticalAlignment(VerticalAlignment.CENTER)
						horizontalAlignment(HorizontalAlignment.LEFT)

						buyButton(5)
						buyButton(16)
						buyButton(64)
					}
				}

				var max = 0
				fun updateMax(stock: Int, forSaleStack: ItemStack) {
					max = min(stock, handler.playerInvSpace(forSaleStack))
				}
				updateMax(handler.stock.get(), handler.forSaleStack.get())
				buyButton("Buy Max ($max)", max) {
					sizing(Sizing.fixed(82), Sizing.fixed(12))
					margins(Insets.of(1))
					fun updateActive(cost: Int, account: PlayerAccount) {
						active = max > 0 && handler.playerCanBuy(max, cost, account)
					}
					updateActive(handler.cost.get(), handler.playerAccount.get())
					updateTooltip(max, handler.playerAccount.get())

					handler.stock.observe {
						updateMax(it, handler.forSaleStack.get())
						updateActive(handler.cost.get(), handler.playerAccount.get())
						updateTooltip(max, handler.playerAccount.get())
						message = Text.of("Buy Max ($max)")
					}
					handler.cost.observe {
						updateActive(it, handler.playerAccount.get())
						updateTooltip(max, handler.playerAccount.get())
					}
					handler.forSaleStack.observe { updateMax(handler.stock.get(), it) }
					handler.playerAccount.observe {
						updateActive(handler.cost.get(), it)
						updateTooltip(max, it)
					}
				}
			}
		}

	private fun buy(num: Int) = handler.sendPurchase(num)
}
