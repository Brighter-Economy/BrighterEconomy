package brightspark.brightereconomy.rest

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.economy.EconomyState
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.minecraft.util.UserCache
import java.util.*

object ApiController {
	private var engine: Optional<NettyApplicationEngine> = Optional.empty()

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
			get("{uuid?}") {
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
			get("{uuid?}") {
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
