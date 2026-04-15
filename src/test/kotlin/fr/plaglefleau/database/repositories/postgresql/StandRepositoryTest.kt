package fr.plaglefleau.database.repositories.postgresql

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StandRepositoryTest : TestDatabaseBase() {

    private val repository = StandRepository()

    @Test
    fun `getStand by id returns stand when it exists`() {
        val result = repository.getStand(1)

        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Stand 1", result.name)
    }

    @Test
    fun `getStand by id returns null when stand does not exist`() {
        val result = repository.getStand(999999)

        assertNull(result)
    }

    @Test
    fun `getStand by name returns stand when it exists`() {
        val result = repository.getStand("Stand 1")

        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Stand 1", result.name)
    }

    @Test
    fun `getStand by name returns null when stand does not exist`() {
        val result = repository.getStand("missing-stand")

        assertNull(result)
    }
}
