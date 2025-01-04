package brightspark.brightereconomy.rest

import brightspark.brightereconomy.BrighterEconomy
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

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
						if (credentials.name == BrighterEconomy.CONFIG.loginUsername()
							&& credentials.password == BrighterEconomy.CONFIG.loginPassword()
						)
							UserIdPrincipal(credentials.name)
						else
							null
					}
				}
			}

			routing {
				authenticate("basic") {
					route("/api", ApiController::routes)
				}

				authenticate("basic", optional = true) {
					get("/api/user-info") {
						call.respondText(call.principal<UserIdPrincipal>()?.name.toString())
					}
				}

				singlePageApplication {
					react("web")
					useResources = true
					defaultPage = "index.html"
				}
//				staticResources("/", "web")

				openAPI(path = "openapi", swaggerFile = "openapi-doc.yaml")
				swaggerUI(path = "swagger", swaggerFile = "openapi-doc.yaml")
			}
		}.start().engine
}
