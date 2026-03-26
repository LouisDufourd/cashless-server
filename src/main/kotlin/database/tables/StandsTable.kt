package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object StandsTable : IntIdTable("stands") {
    // Name of the stand, used in the UI and business logic.
    val name = varchar("name", 45)
}