package brightspark.brightereconomy

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu
import io.wispforest.owo.config.annotation.RangeConstraint
import io.wispforest.owo.config.annotation.RestartRequired

@Config(name = BrighterEconomy.MOD_ID, wrapperName = "ModConfig")
@Modmenu(modId = BrighterEconomy.MOD_ID)
class ModConfigModel {
	@JvmField
	@RestartRequired
	var apiEnabled: Boolean = true

	@JvmField
	@RangeConstraint(min = 0.0, max = 65535.0)
	@RestartRequired
	var apiPort: Int = 25570

	@JvmField
	var whitelist: List<String> = emptyList()

	@JvmField
	var blacklist: List<String> = emptyList()
}
