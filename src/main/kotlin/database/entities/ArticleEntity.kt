package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.ArticlesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class ArticleEntity(id: EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the articles table.
    companion object : IntEntityClass<ArticleEntity>(ArticlesTable)

    // Article name value.
    var name by ArticlesTable.name
}