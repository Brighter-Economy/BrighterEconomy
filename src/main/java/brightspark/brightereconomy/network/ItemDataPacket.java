package brightspark.brightereconomy.network;

import io.wispforest.owo.network.ServerAccess;

import java.util.List;

public record ItemDataPacket(List<ItemData> data) implements ServerPacket {
	@Override
	public void handle(ServerAccess access) {
		ServerPacketHandler.INSTANCE.onItemDataPacket(this);
	}

	public record ItemData(String itemId, String localisedName, byte[] imageBytes) {}
}
