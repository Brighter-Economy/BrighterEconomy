package brightspark.brightereconomy.commands.argtype

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyState
import brightspark.brightereconomy.economy.PlayerAccount
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

class PlayerAccountArgumentType : ArgumentType<PlayerAccountArgument> {
	companion object {
		val ID = Identifier.of(BrighterEconomy.MOD_ID, "player_account")

		fun playerAccountArg() = PlayerAccountArgumentType()

		fun get(context: CommandContext<ServerCommandSource>, name: String): PlayerProfileAndAccount =
			context.getArgument(name, PlayerAccountArgument::class.java).get(context.source)
	}

	override fun parse(reader: StringReader): PlayerAccountArgument {
		val cursorStart = reader.cursor
		while (reader.canRead() && reader.peek() != ' ') {
			reader.skip()
		}

		val string = reader.string.substring(cursorStart, reader.cursor)
		return PlayerAccountArgument { source ->
			source.server.userCache!!.findByName(string)
				.map { PlayerProfileAndAccount(it, EconomyState.get(source.server).getAccount(it.id)) }
				.orElseThrow { EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.createWithContext(reader) }
		}
	}

	override fun <S : Any?> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> = ArgUtil.listGameProfileSuggestions(context, builder)
}

fun interface PlayerAccountArgument : ArgGetter<PlayerProfileAndAccount>

data class PlayerProfileAndAccount(val profile: GameProfile, val account: PlayerAccount)
