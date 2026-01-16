package brightspark.brightereconomy.rest

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.persistance.database.AuthPasswordsDb
import brightspark.brightereconomy.rest.dto.UserAuthInfoDto
import brightspark.brightereconomy.util.Util
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.fabricmc.loader.api.FabricLoader
import java.util.*
import kotlin.jvm.optionals.getOrNull

object RestController {
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
			install(Authentication) {
				basic("basic") {
					realm = "Brighter Economy Dashboard"
					validate { credentials ->
						if (AuthPasswordsDb.checkPassword(credentials.name, credentials.password))
							UserIdPrincipal(credentials.name)
						else
							null
					}
				}
			}

			routing {
				authenticate("basic") {
					get("/user-info") {
						val name = call.principal<UserIdPrincipal>()!!.name
						val playerUuid = Util.getUuid(name).getOrNull() ?: run {
							call.respond(HttpStatusCode.NotFound, "Player does not exist")
							return@get
						}
						call.respond(UserAuthInfoDto(name, playerUuid))
					}
					route("/api") {
						ApiController.routes(this)
					}
				}

				WebController.routes(this)

				if (FabricLoader.getInstance().isDevelopmentEnvironment) {
					SwaggerController.routes(this)
				}
			}
		}.start().engine
}
