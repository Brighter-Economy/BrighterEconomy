package brightspark.brightereconomy.persistance.persistentstate

import brightspark.brightereconomy.persistance.LocalisedNameStorage
import brightspark.brightereconomy.persistance.persistentstate.PersistentStateProvider
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier
import net.minecraft.world.PersistentState

class LocalisedNameState : PersistentState, LocalisedNameStorage {
	companion object : PersistentStateProvider<LocalisedNameState>(
		"localised-names",
		::LocalisedNameState,
		::LocalisedNameState
	)

	private val itemNames = mutableMapOf<Identifier, String>()
	private val enchantmentNames = mutableMapOf<Identifier, String>()

	constructor()

	constructor(nbt: NbtCompound) {
		readNbt(nbt)
	}

	override fun getItemName(id: Identifier): String? = itemNames[id]

	override fun setItemName(id: Identifier, name: String) {
		itemNames[id] = name
	}

	override fun getEnchantmentName(id: Identifier): String? = enchantmentNames[id]

	override fun setEnchantmentName(id: Identifier, name: String) {
		enchantmentNames[id] = name
	}

	private fun readNbt(nbt: NbtCompound) {
		fun readNamesFromNbt(map: MutableMap<Identifier, String>, key: String) {
			map.clear()
			nbt.getList(key, NbtElement.COMPOUND_TYPE.toInt()).forEach {
				val nbtValue = it as NbtCompound
				val id = Identifier(nbtValue.getString("id"))
				val name = nbtValue.getString("name")
				map[id] = name
			}
		}

		readNamesFromNbt(itemNames, "itemNames")
		readNamesFromNbt(enchantmentNames, "enchantmentNames")
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound = nbt.apply {
		fun writeNamesToNbt(map: MutableMap<Identifier, String>, key: String) {
			put(key, NbtList().apply {
				map.entries.forEach {
					add(NbtCompound().apply {
						putString("id", it.key.toString())
						putString("name", it.value)
					})
				}
			})
		}

		writeNamesToNbt(itemNames, "itemNames")
		writeNamesToNbt(enchantmentNames, "enchantmentNames")
	}
}
