@file:UseSerializers(UuidSerializer::class, ItemStackSerializer::class)

package brightspark.brightereconomy.rest.dto

import brightspark.brightereconomy.economy.TransactionParticipants
import brightspark.brightereconomy.economy.TransactionType
import brightspark.brightereconomy.rest.serializer.ItemStackSerializer
import brightspark.brightereconomy.rest.serializer.UuidSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.item.ItemStack
import java.util.*

@Serializable
data class TransactionDto(
	val id: UUID,
	val type: TransactionType,
	val participants: TransactionParticipants,
	val uuidFrom: UUID?,
	val uuidTo: UUID?,
	val nameFrom: String?,
	val nameTo: String?,
	val money: Long,
	val itemPurchased: ItemStack?,
	val timestamp: Long
)
