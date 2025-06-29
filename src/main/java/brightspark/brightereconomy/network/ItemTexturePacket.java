package brightspark.brightereconomy.network;

import io.wispforest.owo.network.ServerAccess;

import java.util.List;

public record ItemTexturePacket(List<ItemTexture> textures) implements ServerPacket {
	@Override
	public void handle(ServerAccess access) {
		ServerPacketHandler.INSTANCE.onItemTexturePacket(this);
	}

	public record ItemTexture(String itemId, byte[] bytes) {}
}
