package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.rest.dto.ShopDto
import brightspark.brightereconomy.shops.ShopTrackerService
import io.ktor.server.plugins.*
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import java.util.*

object ShopService {
	fun getShops(itemId: String?): List<ShopDto> {
		throwIfServerNotAvailable()
		return getShopsSequence(itemId).toList()
	}

	fun getShopsForPlayer(playerUuid: UUID, itemId: String?): List<ShopDto> {
		throwIfServerNotAvailable()
		return getShopsSequence(itemId).filter { it.ownerUuid == playerUuid }.toList()
	}

	private fun getShopsSequence(itemId: String?): Sequence<ShopDto> {
		val itemFilter: Item? = itemId?.let {
			val id = Identifier.tryParse(it) ?: throw BadRequestException("Invalid item ID '$itemId'")
			BrighterEconomy.SERVER.get().registryManager.getOrThrow(RegistryKeys.ITEM).get(id)
				?: throw BadRequestException("Item '$id' does not exist")
		}
		var shops = ShopTrackerService.getShops().asSequence()
		itemFilter?.let { item -> shops = shops.filter { it.itemStack.item == item } }
		return shops.map { it.toDto() }
	}
}
