package brightspark.brightereconomy

import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.commands.BaseCommand
import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.economy.PlayerAccount
import brightspark.brightereconomy.items.ShopBlockItem
import brightspark.brightereconomy.network.ItemDataPacket
import brightspark.brightereconomy.network.ServerPacket
import brightspark.brightereconomy.rest.RestController
import brightspark.brightereconomy.screen.ShopCustomerScreenHandler
import brightspark.brightereconomy.screen.ShopOwnerScreenHandler
import io.wispforest.owo.network.OwoNetChannel
import io.wispforest.owo.network.serialization.PacketBufSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemStack
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
import java.io.File
import java.nio.file.Path
import java.util.*

object BrighterEconomy : ModInitializer {
	const val MOD_ID = "brightereconomy"
	val LOG: Logger = LoggerFactory.getLogger(MOD_ID)
	val CONFIG: ModConfig = ModConfig.createAndLoad()
	val NETWORK: OwoNetChannel = OwoNetChannel.create(id("main"))

	val SERVER_RESOURCES_DIR_PATH: Path = FabricLoader.getInstance().gameDir.resolve("$MOD_ID-resources")
	val SERVER_RESOURCES_DIR_FILE: File = SERVER_RESOURCES_DIR_PATH.toFile()
	var SERVER: Optional<MinecraftServer> = Optional.empty()
		private set

	lateinit var PLAYER_SHOP_BLOCK: ShopBlock
	lateinit var SERVER_SHOP_BLOCK: ShopBlock
	lateinit var SHOP_BLOCK_ENTITY: BlockEntityType<ShopBlockEntity>
	lateinit var SHOP_OWNER_SCREEN_HANDLER: ScreenHandlerType<ShopOwnerScreenHandler>
	lateinit var SHOP_CUSTOMER_SCREEN_HANDLER: ScreenHandlerType<ShopCustomerScreenHandler>

	override fun onInitialize() {
		SERVER_RESOURCES_DIR_FILE.mkdirs()

		// Events
		ServerLifecycleEvents.SERVER_STARTING.register { SERVER = Optional.of(it) }
		ServerLifecycleEvents.SERVER_STARTED.register { RestController.init() }
		ServerLifecycleEvents.SERVER_STOPPING.register { RestController.shutdown() }
		ServerLifecycleEvents.SERVER_STOPPED.register { SERVER = Optional.empty() }

		// Commands
		ArgumentTypeRegistry.registerArgumentType(
			PlayerAccountArgumentType.ID,
			PlayerAccountArgumentType::class.java,
			ConstantArgumentSerializer.of(::PlayerAccountArgumentType)
		)
		ArgumentTypeRegistry.registerArgumentType(
			PlayerProfileArgumentType.ID,
			PlayerProfileArgumentType::class.java,
			ConstantArgumentSerializer.of(::PlayerProfileArgumentType)
		)
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
				.icon { ItemStack(PLAYER_SHOP_BLOCK) }
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
		SHOP_OWNER_SCREEN_HANDLER = regScreenHandler("shop_owner", ::ShopOwnerScreenHandler)
		SHOP_CUSTOMER_SCREEN_HANDLER = regScreenHandler("shop_customer", ::ShopCustomerScreenHandler)

		// Network
		regServerPacket<ItemDataPacket>()
		PacketBufSerializer.register(PlayerAccount::class.java, PlayerAccount.SERIALIZER)
	}

	inline fun <reified T> regServerPacket() where T : Record, T : ServerPacket {
		NETWORK.registerServerbound<T>(T::class.java) { message, access -> message.handle(access) }
	}

	private fun id(name: String): Identifier = Identifier.of(MOD_ID, name)!!

	private fun <T : Block> regBlock(name: String, block: T): T =
		Registry.register(Registries.BLOCK, id(name), block)

	@Suppress("SameParameterValue")
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
