package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object CivilitiesTable : IntIdTable("civilities") {
    // Last name of the person.
    val lastname = varchar("lastname", 45)

    // First name of the person.
    val firstname = varchar("firstname", 45)

    // Age is stored as a simple integer because it is used only as a basic attribute.
    val age = integer("age")

    init {
        // Prevent duplicated civility records with the same firstname, lastname, and age.
        index(columns = arrayOf(lastname, firstname, age), isUnique = true)
    }
}