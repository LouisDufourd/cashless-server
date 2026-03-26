package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object ArticlesTable : IntIdTable("articles") {
    // Article label, kept short because the table stores product-like catalog names.
    val name = varchar("name", 45)
}