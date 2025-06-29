package brightspark.brightereconomy.rest

import brightspark.brightereconomy.rest.dto.Sort
import brightspark.brightereconomy.rest.service.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.util.*

object ApiController {
	fun routes(route: Route): Unit = route.run {
		route("/configs") {
			get {
				call.respond(ConfigService.getConfigs())
			}

			get("/{key}") {
				val key: String by call.parameters
				call.respond(ConfigService.getConfig(key))
			}

			put("/{key}") {
				val key: String by call.parameters
				ConfigService.setConfig(key, call.receiveText())
				call.respond(HttpStatusCode.OK)
			}
		}

		route("/accounts") {
			get {
				call.respond(AccountService.getAccounts())
			}

			route("/{uuid}") {
				get {
					val uuid: UUID by call.parameters
					call.respond(AccountService.getAccount(uuid))
				}

				get("/transactions") {
					val uuid: UUID by call.parameters
					val limit: Int = call.queryParameters.getOptional("limit", 10)
					val sort: Sort = call.queryParameters.getOptional("sort", Sort.DESC)
					call.respond(TransactionService.getTransactionsForPlayer(uuid, limit, sort))
				}

				put("/balance") {
					val uuid: UUID by call.parameters
					val money: Long = call.receive()
					val username: String = call.principal<UserIdPrincipal>()!!.name
					AccountService.setBalance(uuid, money, username)
					call.respond(HttpStatusCode.OK)
				}
			}
		}

		get("/transactions") {
			val limit: Int = call.queryParameters.getOptional("limit", 10)
			val sort: Sort = call.queryParameters.getOptional("sort", Sort.DESC)
			call.respond(TransactionService.getTransactions(limit, sort))
		}

		get("/shops") {
			call.respond(ShopService.getShops())
		}

		route("/items") {
			get {
				call.respond(ItemService.getAllItemKeys())
			}

			get("/{id}/name") {
				val id: String by call.parameters
				call.respond(ItemService.getItemLocalisedName(id))
			}
		}
	}

	private inline fun <reified R : Any> Parameters.getOptional(name: String, default: R): R =
		if (name in this) this.getOrFail<R>(name) else default
}
