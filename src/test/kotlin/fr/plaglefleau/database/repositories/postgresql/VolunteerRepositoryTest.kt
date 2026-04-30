package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.exceptions.AuthenticationException
import fr.plaglefleau.database.exceptions.NotFoundException
import fr.plaglefleau.database.tables.RoleName
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VolunteerRepositoryTest : TestDatabaseBase() {

    private val repository = VolunteerRepository()

    @Test
    fun `login returns volunteer id when credentials are valid`() {
        assertEquals(1, repository.login("volunteer_1", "password123"))
        assertEquals(2, repository.login("volunteer_2", "password123"))
        assertEquals(3, repository.login("volunteer_3", "password123"))
        assertEquals(4, repository.login("volunteer_4", "password123"))
    }

    @Test
    fun `login throws AuthenticationException when credentials are invalid`() {
        assertThrows<AuthenticationException> { repository.login("bad-user", "bad-password") }
        assertThrows<AuthenticationException> { repository.login("volunteer_1", "bad-password") }
        assertThrows<AuthenticationException> { repository.login("bad-user", "password123") }
    }

    @Test
    fun `getRole returns role when volunteer exists`() {
        val result = repository.getRole(1)

        assertEquals(RoleName.ORGANIZER, result)
    }

    @Test
    fun `getRole throws NotFoundException when volunteer does not exist`() {
        assertThrows<NotFoundException> { repository.getRole(999999) }
    }
}
