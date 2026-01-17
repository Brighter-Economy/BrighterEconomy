package brightspark.brightereconomy.persistance.database

import brightspark.brightereconomy.util.Util
import net.minecraft.item.ItemStack
import net.minecraft.nbt.visitor.StringNbtWriter
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import kotlin.jvm.optionals.getOrNull

fun <ID : Any, E : Entity<ID>, EC : EntityClass<ID, E>> updateOrCreate(entity: EC, id: ID, setter: (E) -> Unit): E =
	entity.findByIdAndUpdate(id) { setter(it) } ?: entity.new(id) { setter(this) }

fun <ID : Any, E : Entity<ID>, EC : EntityClass<ID, E>> findOrCreate(entity: EC, id: ID, init: (E.() -> Unit) = {}): E =
	entity.findById(id) ?: entity.new(id, init)

fun itemStackFromDbParts(id: String, count: Int, components: String?): ItemStack =
	ItemStack(Registries.ITEM[Identifier.of(id)], count).also { stack ->
		components?.let { c -> Util.jsonStringToComponents(c).ifPresent { stack.applyComponentsFrom(it) } }
	}

fun ItemStack.toDbParts(): Triple<String, Int, String?> = Triple(
	Registries.ITEM.getKey(this.item).get().value.toString(),
	this.count,
	Util.componentsToJsonString(this.components).getOrNull()
)
