package brightspark.brightereconomy

import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.commands.BaseCommand
import brightspark.brightereconomy.items.ShopBlockItem
import brightspark.brightereconomy.rest.ApiController
import brightspark.brightereconomy.screen.ShopScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
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
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object BrighterEconomy : ModInitializer {
	const val MOD_ID = "brightereconomy"
	val LOG: Logger = LoggerFactory.getLogger(MOD_ID)
	val CONFIG = ModConfig.createAndLoad()
//	val NETWORK = OwoNetChannel.create(id("main"))

	var SERVER: Optional<MinecraftServer> = Optional.empty()
		private set

	lateinit var PLAYER_SHOP_BLOCK: ShopBlock
	lateinit var SERVER_SHOP_BLOCK: ShopBlock
	lateinit var SHOP_BLOCK_ENTITY: BlockEntityType<ShopBlockEntity>
	lateinit var SHOP_SCREEN_HANDLER: ScreenHandlerType<ShopScreenHandler>

	override fun onInitialize() {
		// Events
		ServerLifecycleEvents.SERVER_STARTING.register { SERVER = Optional.of(it) }
		ServerLifecycleEvents.SERVER_STARTED.register { ApiController.init() }
		ServerLifecycleEvents.SERVER_STOPPING.register { ApiController.shutdown() }
		ServerLifecycleEvents.SERVER_STOPPED.register { SERVER = Optional.empty() }

		// Commands
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> BaseCommand.register(dispatcher) }

		// Blocks
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

		// Block Items
		val playerShopBlockItem = regBlockItem("player_shop", PLAYER_SHOP_BLOCK, ::ShopBlockItem)
		val serverShopBlockItem = regBlockItem("server_shop", SERVER_SHOP_BLOCK, ::ShopBlockItem)

		// Item Group
		Registry.register(
			Registries.ITEM_GROUP,
			id("group"),
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

		// Screens
		SHOP_SCREEN_HANDLER = regScreenHandler("shop", ::ShopScreenHandler)

		// Network
//		NETWORK.registerServerbound(SetShopDataPacket::class.java, SetShopDataPacket::handle)
	}

	private fun id(name: String): Identifier = Identifier.of(MOD_ID, name)!!

	private fun <T : Block> regBlock(name: String, block: T): T =
		Registry.register(Registries.BLOCK, id(name), block)

	private fun <T : BlockEntity> regBlockEntity(
		name: String,
		factory: FabricBlockEntityTypeBuilder.Factory<T>,
		vararg blocks: Block
	): BlockEntityType<T> = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		id(name),
		FabricBlockEntityTypeBuilder.create(factory, *blocks).build()
	)

	private fun <T : Item> regItem(name: String, item: T): T =
		Registry.register(Registries.ITEM, id(name), item)

	private fun regBlockItem(
		name: String,
		block: Block,
		blockItem: (Block, Settings) -> BlockItem = ::BlockItem
	): BlockItem = regItem(name, blockItem(block, Settings()))

	private fun <T : ScreenHandler> regScreenHandler(
		name: String,
		factory: ScreenHandlerType.Factory<T>
	): ScreenHandlerType<T> = Registry.register(
		Registries.SCREEN_HANDLER,
		id(name),
		ScreenHandlerType(factory, FeatureFlags.VANILLA_FEATURES)
	)
}
