package brightspark.brightereconomy.items

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.util.sendLiteralOverlayMessage
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.HopperBlockEntity
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.TooltipFlag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class ShopBlockItem(block: Block, settings: Properties) : BlockItem(block, settings) {
	companion object {
		private fun getContainerPos(stack: ItemStack): BlockPos =
			stack.getOrDefault(BrighterEconomy.TARGET_POS_COMPONENT_TYPE, BlockPos.ZERO)

		private fun setContainerPos(stack: ItemStack, pos: BlockPos) {
			stack.set(BrighterEconomy.TARGET_POS_COMPONENT_TYPE, pos)
		}
	}

	override fun canPlace(context: BlockPlaceContext, state: BlockState): Boolean {
		// Check has linked container
		val pos = getContainerPos(context.itemInHand)
		if (pos == BlockPos.ZERO) {
			context.player?.sendLiteralOverlayMessage("No container linked!", ChatFormatting.RED)
			return false
		}
		if (!context.clickedPos.closerThan(pos, 10.0)) {
			context.player?.sendLiteralOverlayMessage("Container is too far away!", ChatFormatting.RED)
			return false
		}
		return super.canPlace(context, state)
	}

	override fun useOn(context: UseOnContext): InteractionResult {
		if (context.player == null) return InteractionResult.FAIL

		if (context.player!!.isShiftKeyDown) {
			// Set linked container to NBT
			val pos = context.clickedPos
			val block = context.level.getBlockState(pos).block
			if (block is ShopBlock) return InteractionResult.FAIL

			val inventory = HopperBlockEntity.getContainerAt(context.level, pos)
			if (inventory == null || inventory is Entity) return InteractionResult.FAIL

			setContainerPos(context.itemInHand, pos)
			context.player!!.sendLiteralOverlayMessage("Shop container set to ${pos.toShortString()}")
			return InteractionResult.SUCCESS
		}

		// The super logic will end up calling canPlace
		return super.useOn(context)
	}

	override fun updateCustomBlockEntityTag(
		pos: BlockPos,
		world: Level,
		player: Player?,
		stack: ItemStack,
		state: BlockState
	): Boolean {
		val result = super.updateCustomBlockEntityTag(pos, world, player, stack, state)

		world.getBlockEntity(pos, BrighterEconomy.SHOP_BLOCK_ENTITY).ifPresent { be ->
			be.setLinkedContainer(getContainerPos(stack))
		}

		return result
	}

	override fun appendHoverText(
		stack: ItemStack,
		context: TooltipContext,
		tooltip: MutableList<Component>,
		type: TooltipFlag
	) {
		super.appendHoverText(stack, context, tooltip, type)
		val containerPos = getContainerPos(stack).let { if (it == BlockPos.ZERO) "<none>" else it.toShortString() }
		tooltip.add(Component.literal("Linked Container: $containerPos").withStyle { it.withColor(ChatFormatting.DARK_GRAY) })
	}
}
