package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.tables.RoleName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VolunteerRepositoryTest : TestDatabaseBase() {

    private val repository = VolunteerRepository()

    @Test
    fun `login returns volunteer id when credentials are valid`() {
        val result = repository.login("volunteer_1", "password123")

        assertEquals(1, result)
    }

    @Test
    fun `login returns null when credentials are invalid`() {
        val result = repository.login("bad-user", "bad-password")

        assertNull(result)
    }

    @Test
    fun `getRole returns role when volunteer exists`() {
        val result = repository.getRole(1)

        assertEquals(RoleName.ORGANIZER, result)
    }

    @Test
    fun `getRole returns null when volunteer does not exist`() {
        val result = repository.getRole(999999)

        assertNull(result)
    }
}
