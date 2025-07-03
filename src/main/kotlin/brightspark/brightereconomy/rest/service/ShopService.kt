package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.rest.dto.ShopDto
import brightspark.brightereconomy.shops.ShopTrackerService
import io.ktor.server.plugins.BadRequestException
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object ShopService {
	fun getShops(itemId: String?): List<ShopDto> {
		throwIfShopTrackerStateNull()
		val itemFilter: Item? = itemId?.let {
			val id = Identifier.tryParse(it) ?: throw BadRequestException("Invalid item ID '$itemId'")
			Registries.ITEM.getOrEmpty(id).orElseThrow { BadRequestException("Item '$id' does not exist") }
		}
		return ShopTrackerService.getShops()
			.let { shops -> itemFilter?.let { item -> shops.filter { it.itemStack.item == item } } ?: shops }
			.map { it.toDto() }
	}
}
