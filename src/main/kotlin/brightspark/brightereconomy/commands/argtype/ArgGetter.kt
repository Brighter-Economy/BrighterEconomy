package brightspark.brightereconomy.commands.argtype

import net.minecraft.commands.CommandSourceStack

fun interface ArgGetter<T> {
	fun get(source: CommandSourceStack): T
}
