package brightspark.brightereconomy.rest

import brightspark.brightereconomy.rest.service.AccountService
import brightspark.brightereconomy.rest.service.ConfigService
import brightspark.brightereconomy.rest.service.ItemService
import brightspark.brightereconomy.rest.service.TransactionService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.util.*

object ApiController {
	fun routes(route: Route): Unit = route.run {
		route("/config") {
			get {
				call.respond(ConfigService.getConfigsString())
			}

			put {
				ConfigService.setConfigs(call.receiveText())
				call.respond(HttpStatusCode.OK)
			}

			put("{key}") {
				val key: String by call.parameters
				ConfigService.setConfig(key, call.receiveText())
				call.respond(HttpStatusCode.OK)
			}
		}

		route("/accounts") {
			get {
				call.respond(AccountService.getAccounts())
			}
			get("{uuid}") {
				val uuid: UUID by call.parameters
				call.respond(AccountService.getAccount(uuid))
			}
		}

		route("/transactions") {
			get {
				val limit: Int = call.queryParameters.getOptional("limit", 10)
				call.respond(TransactionService.getTransactions(limit))
			}
			get("{uuid}") {
				val uuid: UUID by call.parameters
				val limit: Int = call.queryParameters.getOptional("limit", 10)
				call.respond(TransactionService.getTransactionsForPlayer(uuid, limit))
			}
		}

		route("/items") {
			get {
				call.respond(ItemService.getAllItemKeys())
			}
		}
	}

	private inline fun <reified R : Any> Parameters.getOptional(name: String, default: R): R =
		if (name in this) this.getOrFail<R>(name) else default
}
