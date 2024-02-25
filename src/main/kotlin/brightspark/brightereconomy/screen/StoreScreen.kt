package brightspark.brightereconomy.screen

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*

class StoreScreen : BaseOwoScreen<FlowLayout>() {
	override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

	override fun build(root: FlowLayout) {
		root.surface(Surface.VANILLA_TRANSLUCENT)
			.horizontalAlignment(HorizontalAlignment.CENTER)
			.verticalAlignment(VerticalAlignment.CENTER)

		root.child(
			Containers.verticalFlow(Sizing.content(), Sizing.content())
				.child(Components.box(Sizing.fixed(20), Sizing.fixed(20)))
				.padding(Insets.of(10))
				.horizontalAlignment(HorizontalAlignment.CENTER)
		)
	}
}
