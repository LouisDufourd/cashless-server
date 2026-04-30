package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.exceptions.NotFoundException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
    fun `getStand by id throws NotFoundException when stand does not exist`() {
        assertThrows<NotFoundException> { repository.getStand(999999) }
    }

    @Test
    fun `getStand by name returns stand when it exists`() {
        val result = repository.getStand("Stand 1")

        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Stand 1", result.name)
    }

    @Test
    fun `getStand by name throws NotFoundException when stand does not exist`() {
        assertThrows<NotFoundException> { repository.getStand("missing-stand") }
    }
}
