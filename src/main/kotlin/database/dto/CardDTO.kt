package fr.plaglefleau.database.dto

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import java.math.BigDecimal

data class CardDTO(
    val id: Int,
    val pin: Int,
    val balance: Double,
    val nfc: String
) {
    constructor(
        id: EntityID<Int>,
        pin: Int,
        balance: BigDecimal,
        nfc: String
    ): this (id.value, pin, balance.toDouble(), nfc)
}
