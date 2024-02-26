package brightspark.brightereconomy.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos

class ShopBlock(settings: Settings) : BlockWithEntity(settings) {
	companion object {
		private val FACING = Properties.HORIZONTAL_FACING
	}

	override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ShopBlockEntity(pos, state)

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
