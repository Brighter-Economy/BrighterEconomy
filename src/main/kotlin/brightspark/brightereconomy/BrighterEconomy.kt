package brightspark.brightereconomy

import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.blocks.ShopBlockEntity
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object BrighterEconomy : ModInitializer {
	const val MOD_ID = "brightereconomy"
	val LOG: Logger = LoggerFactory.getLogger(MOD_ID)

	lateinit var PLAYER_SHOP_BLOCK: ShopBlock
	lateinit var SERVER_SHOP_BLOCK: ShopBlock
	lateinit var SHOP_BLOCK_ENTITY: BlockEntityType<ShopBlockEntity>

	override fun onInitialize() {
		val shopBlockSettings = AbstractBlock.Settings.create().nonOpaque().allowsSpawning(Blocks::never)
		PLAYER_SHOP_BLOCK = regBlock(
			"player_shop",
			ShopBlock(shopBlockSettings.strength(5.0F, 6.0F))
		)
		SERVER_SHOP_BLOCK = regBlock(
			"server_shop",
			ShopBlock(shopBlockSettings.strength(-1.0F, 3600000.0F).dropsNothing())
		)
		SHOP_BLOCK_ENTITY = regBlockEntity("shop", ::ShopBlockEntity, PLAYER_SHOP_BLOCK, SERVER_SHOP_BLOCK)

		val playerShopBlockItem = regBlockItem("player_shop", PLAYER_SHOP_BLOCK)
		val serverShopBlockItem = regBlockItem("server_shop", SERVER_SHOP_BLOCK)

		Registry.register(
			Registries.ITEM_GROUP,
			Identifier(MOD_ID, "group"),
			FabricItemGroup.builder()
				.icon { ItemStack(Items.GOLD_INGOT) }
				.displayName(Text.translatable("itemGroup.brightereconomy.group"))
				.entries { _, entries ->
					entries.apply {
						add(playerShopBlockItem)
						add(serverShopBlockItem)
					}
				}
				.build()
		)
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

	private fun <T : Item> regItem(name: String, item: T): T =
		Registry.register(Registries.ITEM, Identifier(MOD_ID, name), item)

	private fun regBlockItem(name: String, block: Block): BlockItem = regItem(name, BlockItem(block, Settings()))
}
