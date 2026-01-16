package brightspark.brightereconomy

import brightspark.brightereconomy.persistance.StorageType
import io.wispforest.owo.config.annotation.*

@Suppress("unused")
@Config(name = BrighterEconomy.MOD_ID, wrapperName = "ModConfig")
@Modmenu(modId = BrighterEconomy.MOD_ID)
class ModConfigModel {

	// ==== SERVER API ====

	@SectionHeader("server_api")
	@JvmField
	@RestartRequired
	var apiEnabled: Boolean = true

	@JvmField
	@RangeConstraint(min = 0.0, max = 65535.0)
	@RestartRequired
	var apiPort: Int = 25570

	@JvmField
	@ExcludeFromScreen
	var adminUsernames: List<String> = listOf()

	// ==== ECONOMY ====

	@SectionHeader("economy")
	@JvmField
	@RegexConstraint("^[^\\s]{1,5}$")
	var currencySymbol: String = "£"

	@JvmField
	@RangeConstraint(min = 0.0, max = 0.99)
	var transferTax: Float = 0F

	@JvmField
	@RangeConstraint(min = -1.0, max = Int.MAX_VALUE.toDouble())
	var baseDailyTransferLimit: Int = -1

	// ==== MISC ====

	@SectionHeader("misc")
	@JvmField
	@RestartRequired
	var commandAliases: List<String> = listOf("be")

	@JvmField
	var timeZoneId: String = "UTC"

	// ==== DB CONFIG ====

	@SectionHeader("db")
	@JvmField
	@ExcludeFromScreen
	@RestartRequired
	var dbType: StorageType = StorageType.SQLITE

	@JvmField
	@ExcludeFromScreen
	@RestartRequired
	var dbUrl: String = "brightereconomy.db"

	@JvmField
	@ExcludeFromScreen
	@RestartRequired
	var dbUsername: String = ""

	@JvmField
	@ExcludeFromScreen
	@RestartRequired
	var dbPassword: String = ""
}
