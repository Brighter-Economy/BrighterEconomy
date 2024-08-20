package brightspark.brightereconomy.commands.argtype

import net.minecraft.server.command.ServerCommandSource

fun interface ArgGetter<T> {
	fun get(source: ServerCommandSource): T
}
