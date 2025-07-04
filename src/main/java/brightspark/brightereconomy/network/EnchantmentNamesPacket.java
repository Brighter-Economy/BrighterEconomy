package brightspark.brightereconomy.network;

import io.wispforest.owo.network.ServerAccess;

import java.util.Map;

public record EnchantmentNamesPacket(Map<String, String> data) implements ServerPacket {
	@Override
	public void handle(ServerAccess access) {
		ServerPacketHandler.INSTANCE.onLocalisedNamesPacket(this);
	}
}
