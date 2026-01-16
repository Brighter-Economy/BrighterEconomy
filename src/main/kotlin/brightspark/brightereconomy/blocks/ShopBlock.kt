package brightspark.brightereconomy.blocks

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.shops.ShopTrackerService
import brightspark.brightereconomy.util.sendLiteralOverlayMessage
import com.mojang.serialization.MapCodec
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ShopBlock(settings: Settings) : BlockWithEntity(settings) {
	companion object {
		private val CODEC = createCodec(::ShopBlock)
		private val FACING = Properties.HORIZONTAL_FACING
	}

	override fun getCodec(): MapCodec<ShopBlock> = CODEC

	override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ShopBlockEntity(pos, state)

	override fun <T : BlockEntity?> getTicker(
		world: World?,
		state: BlockState?,
		type: BlockEntityType<T>?
	): BlockEntityTicker<T>? =
		validateTicker(type, BrighterEconomy.SHOP_BLOCK_ENTITY) { w, _, _, be -> be.tick(w) }

	override fun onPlaced(
		world: World,
		pos: BlockPos,
		state: BlockState,
		placer: LivingEntity?,
		itemStack: ItemStack
	) {
		if (!world.isClient) {
			world.getBlockEntity(pos, BrighterEconomy.SHOP_BLOCK_ENTITY).ifPresentOrElse(
				{ be ->
					placer?.uuid?.let { be.setOwner(it) }
					ShopTrackerService.addShop(be)
				},
				{
					BrighterEconomy.LOG.atError()
						.setMessage("Can't get shop block entity when added at {} {}")
						.addArgument(world.dimensionEntry.idAsString).addArgument(pos)
						.log()
				}
			)
		}
		super.onPlaced(world, pos, state, placer, itemStack)
	}

	override fun onStateReplaced(state: BlockState, world: ServerWorld, pos: BlockPos, moved: Boolean) {
		world.getBlockEntity(pos, BrighterEconomy.SHOP_BLOCK_ENTITY).ifPresentOrElse(
			{ ShopTrackerService.removeShop(it) },
			{
				BrighterEconomy.LOG.atError()
					.setMessage("Can't get shop block entity when removed at {} {}")
					.addArgument(world.dimensionEntry.idAsString).addArgument(pos)
					.log()
			}
		)
		super.onStateReplaced(state, world, pos, moved)
	}

	override fun onUse(
		state: BlockState,
		world: World,
		pos: BlockPos,
		player: PlayerEntity,
		hit: BlockHitResult
	): ActionResult {
		if (!world.isClient()) {
			world.getBlockEntity(pos)
				?.takeIf { it is ShopBlockEntity }
				?.let { it as ShopBlockEntity }
				?.let { be ->
					if (be.linkedContainer == BlockPos.ORIGIN)
						player.sendLiteralOverlayMessage("No container linked!", Formatting.RED)
					else
						player.openHandledScreen(state.createScreenHandlerFactory(world, pos))
				}
		}
		return ActionResult.SUCCESS
	}

	override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

	override fun rotate(state: BlockState, rotation: BlockRotation): BlockState =
		state.with(FACING, rotation.rotate(state.get(FACING)))

	override fun mirror(state: BlockState, mirror: BlockMirror): BlockState =
		state.rotate(mirror.getRotation(state.get(FACING)))

	override fun getPlacementState(ctx: ItemPlacementContext): BlockState =
		defaultState.with(FACING, ctx.horizontalPlayerFacing.opposite)

	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		builder.add(FACING)
	}
}
