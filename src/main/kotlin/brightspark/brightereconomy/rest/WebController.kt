package brightspark.brightereconomy.rest

import brightspark.brightereconomy.BrighterEconomy
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

object WebController {
	fun routes(route: Route): Unit = route.run {
		singlePageApplication {
			react("web")
			useResources = true
			defaultPage = "index.html"
		}
		staticFiles("/resources", BrighterEconomy.SERVER_RESOURCES_DIR_FILE)
	}
}
