package brightspark.brightereconomy.network;

import io.wispforest.owo.network.ServerAccess;

public interface ServerPacket {
	void handle(ServerAccess access);
}
