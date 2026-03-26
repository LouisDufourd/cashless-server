package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object RolesTable : IntIdTable("roles") {
    // Human-readable role name such as Admin, Seller, or Manager.
    val name = varchar("name", 45)
}