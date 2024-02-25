package brightspark.brightereconomy

import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.blocks.ShopBlockEntity
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object BrighterEconomy : ModInitializer {
	const val MOD_ID = "brightereconomy"

	lateinit var PLAYER_SHOP_BLOCK: ShopBlock
	lateinit var SERVER_SHOP_BLOCK: ShopBlock
	lateinit var SHOP_BLOCK_ENTITY: BlockEntityType<ShopBlockEntity>

	override fun onInitialize() {
		val shopBlockSettings = AbstractBlock.Settings.create().strength(5.0F, 6.0F)
		PLAYER_SHOP_BLOCK = regBlock("player_shop", ShopBlock(shopBlockSettings))
		SERVER_SHOP_BLOCK = regBlock("server_shop", ShopBlock(shopBlockSettings))
		SHOP_BLOCK_ENTITY = regBlockEntity("shop", ::ShopBlockEntity, PLAYER_SHOP_BLOCK, SERVER_SHOP_BLOCK)
	}

	private fun <T : Block> regBlock(name: String, block: T): T =
		Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block)

	private fun <T : BlockEntity> regBlockEntity(
		name: String,
		factory: FabricBlockEntityTypeBuilder.Factory<T>,
		vararg blocks: Block
	): BlockEntityType<T> = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		Identifier.of(MOD_ID, name),
		FabricBlockEntityTypeBuilder.create(factory, *blocks).build()
	)
}
