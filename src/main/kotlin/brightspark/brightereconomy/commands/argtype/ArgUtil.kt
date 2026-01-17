package brightspark.brightereconomy.commands.argtype

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.selector.EntitySelectorParser
import java.util.concurrent.CompletableFuture

object ArgUtil {
	fun <S : Any?> listGameProfileSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> = if (context.source is SharedSuggestionProvider) {
		val source = context.source as SharedSuggestionProvider
		val stringReader = StringReader(builder.input).apply { cursor = builder.start }
		EntitySelectorParser(stringReader, false)
			.apply { parse() }
			.run { fillSuggestions(builder) { b -> SharedSuggestionProvider.suggest(source.onlinePlayerNames, b) } }

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
