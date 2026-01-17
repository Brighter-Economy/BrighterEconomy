package brightspark.brightereconomy.commands.argtype

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyService
import brightspark.brightereconomy.economy.PlayerAccount
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

class PlayerAccountArgumentType : ArgumentType<PlayerAccountArgument> {
	companion object {
		val ID = ResourceLocation.fromNamespaceAndPath(BrighterEconomy.MOD_ID, "player_account")

		fun playerAccountArg() = PlayerAccountArgumentType()

		fun get(context: CommandContext<CommandSourceStack>, name: String): PlayerProfileAndAccount =
			context.getArgument(name, PlayerAccountArgument::class.java).get(context.source)
	}

	override fun parse(reader: StringReader): PlayerAccountArgument {
		val cursorStart = reader.cursor
		while (reader.canRead() && reader.peek() != ' ') {
			reader.skip()
		}

		val string = reader.string.substring(cursorStart, reader.cursor)
		return PlayerAccountArgument { source ->
			source.server.profileCache!!.get(string)
				.map { PlayerProfileAndAccount(it, EconomyService.getAccount(it.id)) }
				.orElseThrow { EntityArgument.NO_PLAYERS_FOUND.createWithContext(reader) }
		}
	}

	override fun <S : Any?> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> = ArgUtil.listGameProfileSuggestions(context, builder)
}

fun interface PlayerAccountArgument : ArgGetter<PlayerProfileAndAccount>

data class PlayerProfileAndAccount(val profile: GameProfile, val account: PlayerAccount)
