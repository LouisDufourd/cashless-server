package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.tables.RoleName

interface IVolunteerRepository {
    fun login(username: String, password: String): Int?
    fun getRole(volunteerId: Int): RoleName?
}