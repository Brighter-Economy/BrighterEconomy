package brightspark.brightereconomy.commands.argtype

import brightspark.brightereconomy.BrighterEconomy
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class PlayerProfileArgumentType : ArgumentType<PlayerProfileArg> {
	companion object {
		val ID = Identifier.of(BrighterEconomy.MOD_ID, "player_profile")

		fun playerProfileArg() = PlayerProfileArgumentType()

		fun get(context: CommandContext<ServerCommandSource>, name: String): GameProfile =
			context.getArgument(name, PlayerProfileArg::class.java).get(context.source)
	}

	override fun parse(reader: StringReader): PlayerProfileArg {
		val cursorStart = reader.cursor
		while (reader.canRead() && reader.peek() != ' ') {
			reader.skip()
		}

		val string = reader.string.substring(cursorStart, reader.cursor)
		return PlayerProfileArg { source ->
			source.server.userCache!!.findByName(string)
				.orElseThrow { EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.createWithContext(reader) }
		}
	}

	override fun <S : Any?> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> = ArgUtil.listGameProfileSuggestions(context, builder)
}

fun interface PlayerProfileArg : ArgGetter<GameProfile>
