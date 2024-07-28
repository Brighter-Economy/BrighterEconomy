package brightspark.brightereconomy.screen

import io.wispforest.owo.client.screens.SyncedProperty
import io.wispforest.owo.ui.component.*
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

// Flows

fun verticalFlowComponent(
	horizontalSizing: Sizing = Sizing.content(),
	verticalSizing: Sizing = Sizing.content(),
	block: FlowLayout.() -> Unit
): FlowLayout = Containers.verticalFlow(horizontalSizing, verticalSizing).apply(block)

fun FlowLayout.verticalFlow(
	horizontalSizing: Sizing = Sizing.content(),
	verticalSizing: Sizing = Sizing.content(),
	block: FlowLayout.() -> Unit
) {
	this.child(verticalFlowComponent(horizontalSizing, verticalSizing, block))
}

fun horizontalFlowComponent(
	horizontalSizing: Sizing = Sizing.content(),
	verticalSizing: Sizing = Sizing.content(),
	block: FlowLayout.() -> Unit
): FlowLayout = Containers.horizontalFlow(horizontalSizing, verticalSizing).apply(block)

fun FlowLayout.horizontalFlow(
	horizontalSizing: Sizing = Sizing.content(),
	verticalSizing: Sizing = Sizing.content(),
	block: FlowLayout.() -> Unit
) {
	this.child(horizontalFlowComponent(horizontalSizing, verticalSizing, block))
}

fun FlowLayout.grid(
	rows: Int,
	columns: Int,
	horizontalSizing: Sizing = Sizing.content(),
	verticalSizing: Sizing = Sizing.content(),
	block: GridLayout.() -> Unit
) {
	this.child(Containers.grid(horizontalSizing, verticalSizing, rows, columns).apply(block))
}

fun GridLayout.gridChild(row: Int, column: Int, child: Component) {
	this.child(child, row, column)
}

// Components

fun FlowLayout.box(
	horizontalSizing: Sizing = Sizing.content(),
	verticalSizing: Sizing = Sizing.content(),
	block: BoxComponent.() -> Unit = {}
) {
	this.child(Components.box(horizontalSizing, verticalSizing).apply(block))
}

fun FlowLayout.button(text: Text, onPress: (ButtonComponent) -> Unit, block: ButtonComponent.() -> Unit = {}) {
	this.child(Components.button(text, onPress).apply(block))
}

fun labelComponent(text: Text, block: LabelComponent.() -> Unit = {}): LabelComponent =
	Components.label(text).apply { shadow(true) }.apply(block)

fun FlowLayout.label(text: Text, block: LabelComponent.() -> Unit = {}) {
	this.child(labelComponent(text, block))
}

fun <T> FlowLayout.label(
	property: SyncedProperty<T>,
	textFactory: (T) -> Text,
	block: LabelComponent.() -> Unit = {}
) {
	this.child(labelComponent(textFactory(property.get())) {
		property.observe { text(textFactory(it)) }
		block()
	})
}

fun textBoxComponent(
	horizontalSizing: Sizing = Sizing.content(),
	block: TextBoxComponent.() -> Unit = {}
): TextBoxComponent = Components.textBox(horizontalSizing).apply(block)

fun FlowLayout.textBox(horizontalSizing: Sizing = Sizing.content(), block: TextBoxComponent.() -> Unit = {}) {
	this.child(textBoxComponent(horizontalSizing, block))
}

fun FlowLayout.texture(texture: Identifier, width: Int, height: Int, block: TextureComponent.() -> Unit = {}) {
	this.child(Components.texture(texture, 0, 0, width, height, width, height).apply(block))
}

fun itemComponent(stack: ItemStack, block: ItemComponent.() -> Unit = {}): ItemComponent =
	Components.item(stack).apply(block)

fun FlowLayout.item(stack: ItemStack, block: ItemComponent.() -> Unit = {}) {
	this.child(itemComponent(stack, block))
}

fun FlowLayout.item(stackProperty: SyncedProperty<ItemStack>, block: ItemComponent.() -> Unit = {}) {
	this.child(itemComponent(stackProperty.get()) {
		stackProperty.observe { stack(it) }
		block()
	})
}

fun Component.tooltip(vararg text: String) {
	this.tooltip(text.map { Text.of(it) })
}
