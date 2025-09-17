package brightspark.brightereconomy.persistance.database

import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader
import net.minecraft.nbt.visitor.StringNbtWriter
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

fun <ID : Any, E : Entity<ID>, EC : EntityClass<ID, E>> updateOrCreate(entity: EC, id: ID, setter: (E) -> Unit): E =
	entity.findByIdAndUpdate(id) { setter(it) } ?: entity.new(id) { setter(this) }

fun <ID : Any, E : Entity<ID>, EC : EntityClass<ID, E>> findOrCreate(entity: EC, id: ID, init: (E.() -> Unit) = {}): E =
	entity.findById(id) ?: entity.new(id, init)

fun itemStackFromDbParts(id: String, count: Int, nbt: String?): ItemStack = ItemStack(
	Registries.ITEM[Identifier(id)],
	count
).also { stack ->
	nbt?.let { stack.nbt = StringNbtReader.parse(it) }
}

fun ItemStack.toDbParts(): Triple<String, Int, String?> = Triple(
	Registries.ITEM.getKey(this.item).get().value.toString(),
	this.count,
	this.nbt?.run { StringNbtWriter().apply(this) }
)
