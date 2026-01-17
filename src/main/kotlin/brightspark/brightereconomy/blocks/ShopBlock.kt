package brightspark.brightereconomy.blocks

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.shops.ShopTrackerService
import brightspark.brightereconomy.util.sendLiteralOverlayMessage
import com.mojang.serialization.MapCodec
import net.minecraft.ChatFormatting
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation

class ShopBlock(settings: Properties) : BaseEntityBlock(settings) {
	companion object {
		private val FACING = BlockStateProperties.HORIZONTAL_FACING
	}

	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ShopBlockEntity(pos, state)

	override fun <T : BlockEntity> getTicker(
		world: Level?,
		state: BlockState?,
		type: BlockEntityType<T>
	): BlockEntityTicker<T>? =
		createTickerHelper(type, BrighterEconomy.SHOP_BLOCK_ENTITY) { w, _, _, be -> be.tick(w) }

	override fun setPlacedBy(
		world: Level,
		pos: BlockPos,
		state: BlockState,
		placer: LivingEntity?,
		itemStack: ItemStack
	) {
		if (!world.isClientSide) {
			world.getBlockEntity(pos, BrighterEconomy.SHOP_BLOCK_ENTITY).ifPresentOrElse(
				{ be ->
					placer?.uuid?.let { be.setOwner(it) }
					ShopTrackerService.addShop(be)
				},
				{
					BrighterEconomy.LOG.atError()
						.setMessage("Can't get shop block entity when added at {} {}")
						.addArgument(world.dimensionTypeRegistration().registeredName).addArgument(pos)
						.log()
				}
			)
		}
		super.setPlacedBy(world, pos, state, placer, itemStack)
	}

	override fun onRemove(
		state: BlockState,
		world: Level,
		pos: BlockPos,
		newState: BlockState,
		moved: Boolean
	) {
		if (!state.`is`(newState.block)) {
			world.getBlockEntity(pos, BrighterEconomy.SHOP_BLOCK_ENTITY).ifPresentOrElse(
				{ ShopTrackerService.removeShop(it) },
				{
					BrighterEconomy.LOG.atError()
						.setMessage("Can't get shop block entity when removed at {} {}")
						.addArgument(world.dimensionTypeRegistration().registeredName).addArgument(pos)
						.log()
				}
			)
		}
		super.onRemove(state, world, pos, newState, moved)
	}

	override fun useWithoutItem(
		state: BlockState,
		world: Level,
		pos: BlockPos,
		player: Player,
		hit: BlockHitResult
	): InteractionResult {
		if (!world.isClientSide) {
			world.getBlockEntity(pos)
				?.takeIf { it is ShopBlockEntity }
				?.let { it as ShopBlockEntity }
				?.let { be ->
					if (be.linkedContainer == BlockPos.ZERO)
						player.sendLiteralOverlayMessage("No container linked!", ChatFormatting.RED)
					else
						player.openMenu(state.getMenuProvider(world, pos))
				}
		}
		return InteractionResult.SUCCESS
	}

	override fun codec(): MapCodec<out BaseEntityBlock?> = simpleCodec(::ShopBlock)

	override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

	override fun rotate(state: BlockState, rotation: Rotation): BlockState =
		state.setValue(FACING, rotation.rotate(state.getValue(FACING)))

	override fun mirror(state: BlockState, mirror: Mirror): BlockState =
		state.rotate(mirror.getRotation(state.getValue(FACING)))

	override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState =
		defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

	override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
		builder.add(FACING)
	}
}
