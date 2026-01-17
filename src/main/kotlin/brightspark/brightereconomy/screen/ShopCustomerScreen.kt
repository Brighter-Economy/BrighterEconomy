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
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import kotlin.math.min

class ShopCustomerScreen(handler: ShopCustomerScreenHandler, playerInv: Inventory, title: Component) :
	ShopScreen<ShopCustomerScreenHandler>(handler, playerInv, Component.empty()) {

	override fun topHalf(parent: FlowLayout): Unit =
		parent.horizontalFlow(horizontalSizing = Sizing.fixed(18 * 9)) {
			verticalAlignment(VerticalAlignment.CENTER)

			verticalFlow(horizontalSizing = Sizing.fixed(80)) {
				verticalAlignment(VerticalAlignment.CENTER)
				horizontalAlignment(HorizontalAlignment.CENTER)

				label(Component.nullToEmpty("For Sale:"))

				horizontalFlow {
					verticalAlignment(VerticalAlignment.CENTER)
					horizontalAlignment(HorizontalAlignment.CENTER)

					item(menu.forSaleStack) {
						setTooltipFromStack(true)
					}
					label(menu.forSaleStack, { Component.nullToEmpty("x ${it.count}") }) {
						margins(Insets.left(5))
					}
				}

				label(menu.cost, { Component.nullToEmpty("Cost: ${BrighterEconomy.CONFIG.currencySymbol()}$it") })
				label(menu.stock, { Component.nullToEmpty("Stock: $it") })
			}

			verticalFlow(horizontalSizing = Sizing.fixed(82)) {
				verticalAlignment(VerticalAlignment.CENTER)
				horizontalAlignment(HorizontalAlignment.CENTER)

				fun ButtonComponent.updateTooltip(num: Int, account: PlayerAccount) {
					tooltip(
						"Buying: ${num * menu.forSaleStack.get().count}",
						"Cost: ${Util.formatMoney(num * menu.cost.get().toLong())}",
						"Your Balance: ${Util.formatMoney(account.money)}"
					)
				}

				fun FlowLayout.buyButton(text: String, num: Int, block: ButtonComponent.() -> Unit) =
					this.button(Component.nullToEmpty(text), { buy(num) }, block)

				fun FlowLayout.buyButton(text: String, numSupplier: () -> Int, block: ButtonComponent.() -> Unit) =
					this.button(Component.nullToEmpty(text), { buy(numSupplier()) }, block)

				fun FlowLayout.buyButton(num: Int) = buyButton("Buy $num", num) {
					sizing(Sizing.fixed(40), Sizing.fixed(12))
					margins(Insets.of(1))
					val numToBuy = { num * menu.forSaleStack.get().count }
					fun updateActive(stock: Int, numToBuy: Int, cost: Int, account: PlayerAccount) {
						active = stock >= numToBuy && menu.playerCanBuy(numToBuy, cost, account)
					}
					updateActive(menu.stock.get(), numToBuy(), menu.cost.get(), menu.playerAccount.get())
					updateTooltip(num, menu.playerAccount.get())

					menu.forSaleStack.observe { updateTooltip(num, menu.playerAccount.get()) }
					menu.stock.observe {
						updateActive(
							it,
							numToBuy(),
							menu.cost.get(),
							menu.playerAccount.get()
						)
					}
					menu.cost.observe {
						updateActive(menu.stock.get(), numToBuy(), it, menu.playerAccount.get())
						updateTooltip(num, menu.playerAccount.get())
					}
					menu.playerAccount.observe {
						updateActive(menu.stock.get(), numToBuy(), menu.cost.get(), it)
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
				fun maxText(): String = "Buy Max ($max)"
				fun updateMax(stock: Int, forSaleStack: ItemStack) {
					max = min(stock, menu.playerInvSpace(forSaleStack))
				}
				updateMax(menu.stock.get(), menu.forSaleStack.get())
				buyButton(maxText(), { max }) {
					sizing(Sizing.fixed(82), Sizing.fixed(12))
					margins(Insets.of(1))
					fun updateActive(cost: Int, account: PlayerAccount) {
						active = max > 0 && menu.playerCanBuy(max, cost, account)
					}
					updateActive(menu.cost.get(), menu.playerAccount.get())
					updateTooltip(max, menu.playerAccount.get())

					menu.stock.observe {
						updateMax(it, menu.forSaleStack.get())
						updateActive(menu.cost.get(), menu.playerAccount.get())
						updateTooltip(max, menu.playerAccount.get())
						message = Component.nullToEmpty(maxText())
					}
					menu.cost.observe {
						updateActive(it, menu.playerAccount.get())
						updateTooltip(max, menu.playerAccount.get())
					}
					menu.forSaleStack.observe { updateMax(menu.stock.get(), it) }
					menu.playerAccount.observe {
						updateActive(menu.cost.get(), it)
						updateTooltip(max, it)
					}
				}
			}
		}

	private fun buy(num: Int) = menu.sendPurchase(num)
}
