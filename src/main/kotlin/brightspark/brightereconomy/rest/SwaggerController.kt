package brightspark.brightereconomy.rest

import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

object SwaggerController {
	fun routes(routing: Routing) = routing.apply {
		openAPI(path = "openapi", swaggerFile = "openapi-doc.yaml")
		swaggerUI(path = "swagger", swaggerFile = "openapi-doc.yaml")
	}
}
