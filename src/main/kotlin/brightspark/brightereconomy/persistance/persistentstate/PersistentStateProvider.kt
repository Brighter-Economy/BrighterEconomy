package brightspark.brightereconomy.persistance.persistentstate

import brightspark.brightereconomy.BrighterEconomy
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World

abstract class PersistentStateProvider<T : PersistentState>(
	private val name: String,
	private val supplier: () -> T,
	private val nbtReader: (NbtCompound) -> T
) {
	fun get(): T = BrighterEconomy.SERVER.map { get(it) }.orElseThrow()

	fun get(server: MinecraftServer): T =
		server.getWorld(World.OVERWORLD)!!.persistentStateManager
			.getOrCreate(nbtReader, supplier, "${BrighterEconomy.MOD_ID}.$name")
}