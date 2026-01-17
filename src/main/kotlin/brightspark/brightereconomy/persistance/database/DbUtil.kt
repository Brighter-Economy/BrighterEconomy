package brightspark.brightereconomy.persistance.database

import brightspark.brightereconomy.util.Util
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.StringTagVisitor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import kotlin.jvm.optionals.getOrNull

fun <ID : Any, E : Entity<ID>, EC : EntityClass<ID, E>> updateOrCreate(entity: EC, id: ID, setter: (E) -> Unit): E =
	entity.findByIdAndUpdate(id) { setter(it) } ?: entity.new(id) { setter(this) }

fun <ID : Any, E : Entity<ID>, EC : EntityClass<ID, E>> findOrCreate(entity: EC, id: ID, init: (E.() -> Unit) = {}): E =
	entity.findById(id) ?: entity.new(id, init)

fun itemStackFromDbParts(id: String, count: Int, components: String?): ItemStack =
	ItemStack(BuiltInRegistries.ITEM[ResourceLocation.parse(id)], count).also { stack ->
		components?.let { c -> Util.jsonStringToComponents(c).ifPresent { stack.applyComponents(it) } }
	}

fun ItemStack.toDbParts(): Triple<String, Int, String?> = Triple(
	BuiltInRegistries.ITEM.getResourceKey(this.item).get().location().toString(),
	this.count,
	Util.componentsToJsonString(this.components).getOrNull()
)
