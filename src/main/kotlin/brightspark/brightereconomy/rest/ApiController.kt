package brightspark.brightereconomy.rest

import brightspark.brightereconomy.rest.service.AccountService
import brightspark.brightereconomy.rest.service.ConfigService
import brightspark.brightereconomy.rest.service.TransactionService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

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
				ConfigService.setConfig(call.parameters.getOrFail("key"), call.receiveText())
				call.respond(HttpStatusCode.OK)
			}
		}

		route("/accounts") {
			get {
				call.respond(AccountService.getAccounts())
			}
			get("{uuid}") {
				call.respond(AccountService.getAccount(call.parameters.getOrFail("uuid")))
			}
		}

		route("/transactions") {
			get {
				call.respond(TransactionService.getTransactions())
			}
			get("{uuid}") {
				call.respond(TransactionService.getTransactionsForPlayer(call.parameters.getOrFail("uuid")))
			}
		}
	}
}
