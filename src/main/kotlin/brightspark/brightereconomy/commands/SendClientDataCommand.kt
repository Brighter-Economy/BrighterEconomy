package brightspark.brightereconomy.commands

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.network.ClientDataRequestPacket
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource

object SendClientDataCommand : Command("sendClientData", {
	requiresPermission("sendClientData", PermissionLevel.OP)

	executes { ctx -> SendClientDataCommand.sendClientData(ctx) }
}) {
	private fun sendClientData(ctx: CommandContext<ServerCommandSource>): Int {
		return ctx.source.player?.let {
			BrighterEconomy.NETWORK.serverHandle(it).send(ClientDataRequestPacket())
			return@let 1
		} ?: 0
	}
}
