package brightspark.brightereconomy.items

import brightspark.brightereconomy.BrighterEconomy
import brightspark.brightereconomy.blocks.ShopBlock
import brightspark.brightereconomy.util.sendLiteralOverlayMessage
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtLong
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ShopBlockItem(block: Block, settings: Settings) : BlockItem(block, settings) {
	companion object {
		private const val NBT_CONTAINER = "container"

		private fun getContainerPos(stack: ItemStack): BlockPos? =
			stack.nbt?.getLong(NBT_CONTAINER)?.let { BlockPos.fromLong(it) }
	}

	override fun canPlace(context: ItemPlacementContext, state: BlockState): Boolean {
		// Check has linked container
		getContainerPos(context.stack)?.let { containerPos ->
			if (!context.blockPos.isWithinDistance(containerPos, 10.0)) {
				context.player?.sendLiteralOverlayMessage("Container is too far away!", Formatting.RED)
				return false
			}
		} ?: run {
			context.player?.sendLiteralOverlayMessage("No container linked!", Formatting.RED)
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

			context.stack.setSubNbt(NBT_CONTAINER, NbtLong.of(pos.asLong()))
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
			stack.nbt?.getLong(NBT_CONTAINER)?.let { be.setLinkedContainer(BlockPos.fromLong(it)) }
		}

		return result
	}

	override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
		super.appendTooltip(stack, world, tooltip, context)
		val containerPos = stack.nbt?.getLong(NBT_CONTAINER)
			?.let { BlockPos.fromLong(it).toShortString() }
			?: "<none>"
		tooltip.add(Text.literal("Linked Container: $containerPos").styled { it.withColor(Formatting.DARK_GRAY) })
	}
}
