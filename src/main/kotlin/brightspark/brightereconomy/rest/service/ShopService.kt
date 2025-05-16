package brightspark.brightereconomy.rest.service

import brightspark.brightereconomy.rest.dto.ShopDto
import brightspark.brightereconomy.shops.ShopTrackerService

object ShopService {
	fun getShops(): List<ShopDto> {
		throwIfShopTrackerStateNull()
		return ShopTrackerService.getShops().map { it.toDto() }
	}
}
