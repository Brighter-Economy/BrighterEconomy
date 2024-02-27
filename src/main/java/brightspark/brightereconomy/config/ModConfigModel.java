package brightspark.brightereconomy.config;

import io.wispforest.owo.config.annotation.RangeConstraint;

import java.util.Collections;
import java.util.List;

// Disabled till I can get this working nicely with Kotlin
@SuppressWarnings("unused")
//@Config(name = BrighterEconomy.MOD_ID, wrapperName = "ModConfig")
//@Modmenu(modId = BrighterEconomy.MOD_ID)
public class ModConfigModel {
	public boolean apiEnabled = true;

	@RangeConstraint(min = 0, max = 65535)
	public int apiPort = 25570;

	public List<String> whitelist = Collections.emptyList();

	public List<String> blacklist = Collections.emptyList();
}
