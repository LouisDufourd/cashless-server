package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.exceptions.NotFoundException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRepositoryTest : TestDatabaseBase() {

    private val repository = UserRepository()

    @Test
    fun `getUser by id returns user when it exists`() {
        val result = repository.getUser(1)
        assertEquals(1, result.id.value)
    }

    @Test
    fun `getUser by id throws NotFoundException when user does not exist`() {
        assertThrows<NotFoundException> { repository.getUser(999999) }
    }

    @Test
    fun `getUser by username throws NotFoundException when user does not exist`() {
        assertThrows<NotFoundException> { repository.getUser("missing-user") }
    }

    @Test
    fun `getUser by username returns user when it exists`() {
        val result = repository.getUser("user_1")
        assertEquals(1, result.id.value)
    }
}
