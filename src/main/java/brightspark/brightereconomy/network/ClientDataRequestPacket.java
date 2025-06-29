package brightspark.brightereconomy.network;

import io.wispforest.owo.network.ClientAccess;

public record ClientDataRequestPacket() implements ClientPacket {
	@Override
	public void handle(ClientAccess access) {
		ClientPacketHandler.INSTANCE.onClientDataRequestPacket(access);
	}
}
