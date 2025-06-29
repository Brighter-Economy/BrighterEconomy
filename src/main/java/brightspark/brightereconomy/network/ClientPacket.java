package brightspark.brightereconomy.network;

import io.wispforest.owo.network.ClientAccess;

public interface ClientPacket {
	void handle(ClientAccess access);
}
