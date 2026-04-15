package fr.plaglefleau.database.repositories.postgresql

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest : TestDatabaseBase() {

    private val repository = UserRepository()

    @Test
    fun `getUser by id returns user when it exists`() {
        val result = repository.getUser(1)

        assertNotNull(result)
    }

    @Test
    fun `getUser by id returns null when user does not exist`() {
        val result = repository.getUser(999999)

        assertNull(result)
    }

    @Test
    fun `getUser by username returns user when it exists`() {
        val result = repository.getUser("user_1")

        assertNotNull(result)
    }

    @Test
    fun `getUser by username returns null when user does not exist`() {
        val result = repository.getUser("missing-user")

        assertNull(result)
    }
}
