package brightspark.brightereconomy.rest

import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

object SwaggerSupport {
	fun swaggerRoutes(routing: Routing) = routing.apply {
		openAPI(path = "openapi", swaggerFile = "openapi-doc.yaml")
		swaggerUI(path = "swagger", swaggerFile = "openapi-doc.yaml")
	}
}
