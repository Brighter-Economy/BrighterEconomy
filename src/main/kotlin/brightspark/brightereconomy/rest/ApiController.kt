package brightspark.brightereconomy.rest

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyState
import com.google.gson.Gson
import com.google.gson.JsonArray
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.wispforest.owo.config.Option
import net.minecraft.util.JsonHelper
import net.minecraft.util.UserCache
import java.util.*

object ApiController {
	private var engine: Optional<NettyApplicationEngine> = Optional.empty()
	private val gson = Gson()

	fun init() {
		if (!BrighterEconomy.CONFIG.apiEnabled() || engine.isPresent) return
		BrighterEconomy.LOG.atInfo()
			.setMessage("Starting REST server on port ${BrighterEconomy.CONFIG.apiPort()}")
			.log()
		engine = Optional.of(create())
	}

	fun shutdown() {
		if (engine.isEmpty) return
		BrighterEconomy.LOG.atInfo().setMessage("Stopping REST server").log()
		engine.get().stop()
		engine = Optional.empty()
	}

	private fun create(): NettyApplicationEngine =
		embeddedServer(Netty, port = BrighterEconomy.CONFIG.apiPort()) {
			install(ContentNegotiation) { json() }
			routes()
		}.start()

	private fun Application.routes() = routing {
		route("/config") {
			get {
				val json = com.google.gson.JsonObject()
				BrighterEconomy.CONFIG.allOptions().values.forEach { option ->
					val name = option.key().name()
					when (val value = option.value()) {
						is Boolean -> json.addProperty(name, value)
						is Number -> json.addProperty(name, value)
						is String -> json.addProperty(name, value)
						is List<*> -> {
							when {
								value.isEmpty() -> json.add(name, JsonArray())
								value.first() is String -> json.add(name, JsonArray().also { array ->
									value.forEach { array.add(it as String) }
								})
								else -> BrighterEconomy.LOG.warn(
									"Config '{}' list value of type {} isn't supported",
									name, value::class.typeParameters.first().name
								)
							}
						}
						else -> BrighterEconomy.LOG.warn(
							"Config '{}' value of type {} isn't supported",
							name, value::class.qualifiedName
						)
					}
				}
				call.respond(JsonHelper.toSortedString(json))
			}

			put {
				val json = JsonHelper.deserialize(call.receiveText())
				json.entrySet().forEach { (key, value) ->
					BrighterEconomy.CONFIG.optionForKey<Any>(Option.Key(key))?.set(value)
				}
				call.respond(HttpStatusCode.OK)
			}

			put("{key}") {
				val key = call.parameters["key"]!!
				val value = call.receiveText()
				BrighterEconomy.CONFIG.optionForKey<Any>(Option.Key(key))?.let {
					it.set(value)
					call.respond(HttpStatusCode.OK)
				} ?: run {
					call.respond(HttpStatusCode.OK)
				}
			}
		}

		route("/accounts") {
			fun getUserCache(): Optional<UserCache> =
				Optional.ofNullable(BrighterEconomy.SERVER.get().userCache)

			fun Optional<UserCache>.getUsername(uuid: UUID): String =
				this.flatMap { it.getByUuid(uuid) }.map { it.name }.orElse("")

			get {
				val state = EconomyState.getOptional()
				if (state.isPresent) {
					val userCache = getUserCache()
					call.respond(
						state.get().getAccounts().asSequence()
							.map { it.toDto(userCache.getUsername(it.uuid)) }
							.toList()
					)
				} else
					call.respondText("MinecraftServer not available", status = HttpStatusCode.InternalServerError)
			}
			get("{uuid}") {
				val uuid = call.parameters["uuid"]?.let { UUID.fromString(it) }
					?: return@get call.respondText("Missing UUID", status = HttpStatusCode.BadRequest)
				val state = EconomyState.getOptional()
				if (state.isPresent) {
					val userCache = getUserCache()
					call.respond(state.get().getAccount(uuid).toDto(userCache.getUsername(uuid)))
				} else
					call.respondText("MinecraftServer not available", status = HttpStatusCode.InternalServerError)
			}
		}

		route("/transactions") {
			get {
				val state = EconomyState.getOptional()
				if (state.isPresent)
					call.respond(state.get().getTransactions().toTypedArray())
				else
					call.respondText("MinecraftServer not available", status = HttpStatusCode.InternalServerError)
			}
			get("{uuid}") {
				val uuid = call.parameters["uuid"]?.let { UUID.fromString(it) }
					?: return@get call.respondText("Missing UUID", status = HttpStatusCode.BadRequest)
				val state = EconomyState.getOptional()
				if (state.isPresent)
					call.respond(state.get().getAccountTransactions(uuid))
				else
					call.respondText("MinecraftServer not available", status = HttpStatusCode.InternalServerError)
			}
		}
	}
}
