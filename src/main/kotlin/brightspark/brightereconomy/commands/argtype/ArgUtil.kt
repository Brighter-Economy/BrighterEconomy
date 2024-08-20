package brightspark.brightereconomy.commands.argtype

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.EntitySelectorReader
import java.util.concurrent.CompletableFuture

object ArgUtil {
	fun <S : Any?> listGameProfileSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> = if (context.source is CommandSource) {
		val source = context.source as CommandSource
		val stringReader = StringReader(builder.input).apply { cursor = builder.start }
		EntitySelectorReader(stringReader, false)
			.apply { read() }
			.run { listSuggestions(builder) { b -> CommandSource.suggestMatching(source.playerNames, b) } }

//		@Suppress("CAST_NEVER_SUCCEEDS")
//		val userCache = source.server.userCache as UserCacheMixin
//		CommandSource.suggestMatching(
//			userCache.gameProfiles.toList(),
//			builder,
//			{ it.name ?: it.id.toString() },
//			{ Text.of(it.id.toString()) }
//		)
	} else {
		Suggestions.empty()
	}
}
