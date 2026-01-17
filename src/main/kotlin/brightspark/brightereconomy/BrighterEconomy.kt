package brightspark.brightereconomy

import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.blocks.ShopBlockEntity
import brightspark.brightereconomy.commands.BaseCommand
import brightspark.brightereconomy.commands.argtype.PlayerAccountArgumentType
import brightspark.brightereconomy.commands.argtype.PlayerProfileArgumentType
import brightspark.brightereconomy.items.ShopBlockItem
import brightspark.brightereconomy.network.EnchantmentNamesPacket
import brightspark.brightereconomy.network.ItemDataPacket
import brightspark.brightereconomy.network.ServerPacket
import brightspark.brightereconomy.persistance.database.DbConnection
import brightspark.brightereconomy.rest.RestController
import brightspark.brightereconomy.screen.ShopCustomerScreenHandler
import brightspark.brightereconomy.screen.ShopOwnerScreenHandler
import io.wispforest.owo.network.OwoNetChannel
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.commands.synchronization.SingletonArgumentInfo
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.server.MinecraftServer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.BlockPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.time.ZoneId
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

	lateinit var TARGET_POS_COMPONENT_TYPE: DataComponentType<BlockPos>
	lateinit var PLAYER_SHOP_BLOCK: ShopBlock
	lateinit var SERVER_SHOP_BLOCK: ShopBlock
	lateinit var SHOP_BLOCK_ENTITY: BlockEntityType<ShopBlockEntity>
	lateinit var SHOP_OWNER_SCREEN_HANDLER: MenuType<ShopOwnerScreenHandler>
	lateinit var SHOP_CUSTOMER_SCREEN_HANDLER: MenuType<ShopCustomerScreenHandler>

	val TIME_ZONE_ID: ZoneId
		get() = ZoneId.of(CONFIG.timeZoneId())

	override fun onInitialize() {
		SERVER_RESOURCES_DIR_FILE.mkdirs()
		validateConfig()

		// Events
		ServerLifecycleEvents.SERVER_STARTING.register { SERVER = Optional.of(it) }
		ServerLifecycleEvents.SERVER_STARTED.register {
			DbConnection.connect()
			RestController.init()
		}
		ServerLifecycleEvents.SERVER_STOPPING.register { RestController.shutdown() }
		ServerLifecycleEvents.SERVER_STOPPED.register { SERVER = Optional.empty() }

		// Commands
		ArgumentTypeRegistry.registerArgumentType(
			PlayerAccountArgumentType.ID,
			PlayerAccountArgumentType::class.java,
			SingletonArgumentInfo.contextFree(::PlayerAccountArgumentType)
		)
		ArgumentTypeRegistry.registerArgumentType(
			PlayerProfileArgumentType.ID,
			PlayerProfileArgumentType::class.java,
			SingletonArgumentInfo.contextFree(::PlayerProfileArgumentType)
		)
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> BaseCommand.register(dispatcher) }

		// Data Components
		TARGET_POS_COMPONENT_TYPE =
			regDataComponent("target_pos", DataComponentType.builder<BlockPos>().persistent(BlockPos.CODEC).build())

		// Blocks
		val shopBlockSettings = BlockBehaviour.Properties.of().noOcclusion().isValidSpawn(Blocks::never)
		PLAYER_SHOP_BLOCK = regBlock(
			"player_shop",
			ShopBlock(shopBlockSettings.strength(5.0F, 6.0F))
		)
		SERVER_SHOP_BLOCK = regBlock(
			"server_shop",
			ShopBlock(shopBlockSettings.strength(-1.0F, 3600000.0F).noLootTable())
		)
		SHOP_BLOCK_ENTITY = regBlockEntity("shop", ::ShopBlockEntity, PLAYER_SHOP_BLOCK, SERVER_SHOP_BLOCK)

		// Block Items
		val playerShopBlockItem = regBlockItem("player_shop", PLAYER_SHOP_BLOCK, ::ShopBlockItem)
		val serverShopBlockItem = regBlockItem("server_shop", SERVER_SHOP_BLOCK, ::ShopBlockItem)

		// Item Group
		Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			id("group"),
			FabricItemGroup.builder()
				.icon { ItemStack(PLAYER_SHOP_BLOCK) }
				.title(Component.translatable("itemGroup.brightereconomy.group"))
				.displayItems { _, entries ->
					entries.apply {
						accept(playerShopBlockItem)
						accept(serverShopBlockItem)
					}
				}
				.build()
		)

		// Screens
		SHOP_OWNER_SCREEN_HANDLER = regScreenHandler("shop_owner", ::ShopOwnerScreenHandler)
		SHOP_CUSTOMER_SCREEN_HANDLER = regScreenHandler("shop_customer", ::ShopCustomerScreenHandler)

		// Network
		regServerPacket<ItemDataPacket>()
		regServerPacket<EnchantmentNamesPacket>()
//		PacketBufSerializer.register(PlayerAccount::class.java, PlayerAccount.SERIALIZER)
	}

	private fun validateConfig() {
		TIME_ZONE_ID // Validate time zone
	}

	inline fun <reified T> regServerPacket() where T : Record, T : ServerPacket {
		NETWORK.registerServerbound<T>(T::class.java) { message, access -> message.handle(access) }
	}

	fun id(name: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, name)!!

	private fun <T> regDataComponent(name: String, componentType: DataComponentType<T>): DataComponentType<T> =
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id(name), componentType)

	private fun <T : Block> regBlock(name: String, block: T): T =
		Registry.register(BuiltInRegistries.BLOCK, id(name), block)

	@Suppress("SameParameterValue")
	private fun <T : BlockEntity> regBlockEntity(
		name: String,
		factory: BlockEntityType.BlockEntitySupplier<T>,
		vararg blocks: Block
	): BlockEntityType<T> = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		id(name),
		BlockEntityType.Builder.of(factory, *blocks).build()
	)

	private fun <T : Item> regItem(name: String, item: T): T =
		Registry.register(BuiltInRegistries.ITEM, id(name), item)

	private fun regBlockItem(
		name: String,
		block: Block,
		blockItem: (Block, Properties) -> BlockItem = ::BlockItem
	): BlockItem = regItem(name, blockItem(block, Properties()))

	private fun <T : AbstractContainerMenu> regScreenHandler(
		name: String,
		factory: MenuType.MenuSupplier<T>
	): MenuType<T> = Registry.register(
		BuiltInRegistries.MENU,
		id(name),
		MenuType(factory, FeatureFlags.VANILLA_SET)
	)
}
