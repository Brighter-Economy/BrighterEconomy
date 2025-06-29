package brightspark.brightereconomy.persistance.miscstate

import brightspark.brightereconomy.persistance.ItemLocalisedNameStorage
import brightspark.brightereconomy.persistance.PersistentStateProvider
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier
import net.minecraft.world.PersistentState

class ItemLocalisedNameState : PersistentState, ItemLocalisedNameStorage {
	companion object : PersistentStateProvider<ItemLocalisedNameState>(
		"item-localised-names",
		::ItemLocalisedNameState,
		::ItemLocalisedNameState
	)

	private val names = mutableMapOf<Identifier, String>()

	constructor()

	constructor(nbt: NbtCompound) {
		readNbt(nbt)
	}

	override fun getName(id: Identifier): String? = names[id]

	override fun setName(id: Identifier, name: String) {
		names[id] = name
	}

	private fun readNbt(nbt: NbtCompound) {
		names.clear()
		nbt.getList("names", NbtElement.COMPOUND_TYPE.toInt()).forEach {
			val nbtValue = it as NbtCompound
			val id = Identifier(nbtValue.getString("id"))
			val name = nbtValue.getString("name")
			names[id] = name
		}
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		put("names", NbtList().apply {
			names.entries.forEach {
				add(NbtCompound().apply {
					putString("id", it.key.toString())
					putString("name", it.value)
				})
			}
		})
	}
}