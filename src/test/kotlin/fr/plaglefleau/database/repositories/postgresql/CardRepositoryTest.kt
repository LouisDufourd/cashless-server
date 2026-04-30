package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.exceptions.NotFoundException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class CardRepositoryTest: TestDatabaseBase() {

    private val repository = CardRepository()

    @Test
    fun `getCard by id throws NotFoundException when card does not exist`() {
        assertThrows<NotFoundException> {
            repository.getCard(999999)
        }
    }

    @Test
    fun `getCard by nfc throws NotFoundException when card does not exist`() {
        assertThrows<NotFoundException> {
            repository.getCard("missing-nfc")
        }
    }

    @Test
    fun `getBalance by id returns balance when card exists`() {
        val result = repository.getBalance(1)

        assertEquals(20.0, result)
    }

    @Test
    fun `getBalance by nfc returns balance when card exists`() {
        val result = repository.getBalance("card-nfc")

        assertEquals(20.0, result)
    }

    @Test
    fun `create inserts new card`() {
        repository.create(1234, "new-card-nfc")

        val result = repository.getCard("new-card-nfc")

        assert(result.nfc == "new-card-nfc" && result.pin == 1234)
    }

    @Test
    fun `update by id changes pin and balance`() {
        repository.update(1, pin = 4321, amount = 50.0)

        val result = repository.getCard(1)

        assertEquals(50.0, result.balance)
        assertEquals(4321, result.pin)
    }

    @Test
    fun `update by nfc changes pin and balance`() {
        repository.update("card-nfc", pin = 4321, amount = 50.0)

        val result = repository.getCard("card-nfc")

        assertEquals(50.0, result.balance)
        assertEquals(4321, result.pin)
    }

    @Test
    fun `credit by id increases balance`() {
        repository.credit(1, 10.0)

        val result = repository.getBalance(1)

        assertEquals(30.0, result)
    }

    @Test
    fun `credit by nfc increases balance`() {
        repository.credit("card-nfc", 10.0)

        val result = repository.getBalance("card-nfc")

        assertEquals(30.0, result)
    }

    @Test
    fun `debit by id decreases balance`() {
        repository.debit(1, 5.0, "Stand 1")

        val result = repository.getBalance(1)

        assertEquals(15.0, result)
    }

    @Test
    fun `debit by nfc decreases balance`() {
        repository.debit("card-nfc", 5.0, "Stand 1")

        val result = repository.getBalance("card-nfc")

        assertEquals(15.0, result)
    }

    @Test
    fun `connect card by id and user id`() {
        repository.connect(1, 1)

        val result = repository.getCard(1)

        assertEquals(result.userId, 1)
    }

    @Test
    fun `connect card by id and username`() {
        repository.connect(1, "user_1")

        val result = repository.getCard(1)

        assertEquals(result.userId, 1)
    }

    @Test
    fun `connect card by nfc and user id`() {
        repository.connect("card-nfc", 1)

        val result = repository.getCard("card-nfc")

        assertEquals(result.userId, 1)
    }

    @Test
    fun `connect card by nfc and username`() {
        repository.connect("card-nfc", "user_1")

        val result = repository.getCard("card-nfc")

        assertEquals(result.userId, 1)
    }
}
