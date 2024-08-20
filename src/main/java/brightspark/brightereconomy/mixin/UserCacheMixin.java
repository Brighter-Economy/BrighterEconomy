package brightspark.brightereconomy.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.stream.Stream;

@Mixin(UserCache.class)
public class UserCacheMixin {
	@Shadow
	@Final
	private Map<String, UserCache.Entry> byName;

	@Unique
	public Stream<GameProfile> getGameProfiles() {
		return byName.values().stream().map(UserCache.Entry::getProfile);
	}
}
