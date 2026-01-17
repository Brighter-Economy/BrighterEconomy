package brightspark.brightereconomy.items

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.util.sendLiteralOverlayMessage
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ShopBlockItem(block: Block, settings: Settings) : BlockItem(block, settings) {
	companion object {
		private fun getContainerPos(stack: ItemStack): BlockPos =
			stack.getOrDefault(BrighterEconomy.TARGET_POS_COMPONENT_TYPE, BlockPos.ORIGIN)

		private fun setContainerPos(stack: ItemStack, pos: BlockPos) {
			stack.set(BrighterEconomy.TARGET_POS_COMPONENT_TYPE, pos)
		}
	}

	override fun canPlace(context: ItemPlacementContext, state: BlockState): Boolean {
		// Check has linked container
		val pos = getContainerPos(context.stack)
		if (pos == BlockPos.ORIGIN) {
			context.player?.sendLiteralOverlayMessage("No container linked!", Formatting.RED)
			return false
		}
		if (!context.blockPos.isWithinDistance(pos, 10.0)) {
			context.player?.sendLiteralOverlayMessage("Container is too far away!", Formatting.RED)
			return false
		}
		return super.canPlace(context, state)
	}

	override fun useOnBlock(context: ItemUsageContext): ActionResult {
		if (context.player == null) return ActionResult.FAIL

		if (context.player!!.isSneaking) {
			// Set linked container to NBT
			val pos = context.blockPos
			val block = context.world.getBlockState(pos).block
			if (block is ShopBlock) return ActionResult.FAIL

			val inventory = HopperBlockEntity.getInventoryAt(context.world, pos)
			if (inventory == null || inventory is Entity) return ActionResult.FAIL

			setContainerPos(context.stack, pos)
			context.player!!.sendLiteralOverlayMessage("Shop container set to ${pos.toShortString()}")
			return ActionResult.SUCCESS
		}

		// The super logic will end up calling canPlace
		return super.useOnBlock(context)
	}

	override fun postPlacement(
		pos: BlockPos,
		world: World,
		player: PlayerEntity?,
		stack: ItemStack,
		state: BlockState
	): Boolean {
		val result = super.postPlacement(pos, world, player, stack, state)

		world.getBlockEntity(pos, BrighterEconomy.SHOP_BLOCK_ENTITY).ifPresent { be ->
			be.setLinkedContainer(getContainerPos(stack))
		}

		return result
	}

	override fun appendTooltip(
		stack: ItemStack,
		context: TooltipContext,
		tooltip: MutableList<Text>,
		type: TooltipType
	) {
		super.appendTooltip(stack, context, tooltip, type)
		val containerPos = getContainerPos(stack).let { if (it == BlockPos.ORIGIN) "<none>" else it.toShortString() }
		tooltip.add(Text.literal("Linked Container: $containerPos").styled { it.withColor(Formatting.DARK_GRAY) })
	}
}
