package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.rest.dto.ShopDto
import brightspark.brightereconomy.shops.ShopTrackerService
import io.ktor.server.plugins.BadRequestException
import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

object ShopService {
	fun getShops(itemId: String?): List<ShopDto> {
		throwIfShopTrackerStateNull()
		val itemFilter: Item? = itemId?.let {
			val id = ResourceLocation.tryParse(it) ?: throw BadRequestException("Invalid item ID '$itemId'")
			BuiltInRegistries.ITEM.getOptional(id).orElseThrow { BadRequestException("Item '$id' does not exist") }
		}
		return ShopTrackerService.getShops()
			.let { shops -> itemFilter?.let { item -> shops.filter { it.itemStack.item == item } } ?: shops }
			.map { it.toDto() }
	}
}
