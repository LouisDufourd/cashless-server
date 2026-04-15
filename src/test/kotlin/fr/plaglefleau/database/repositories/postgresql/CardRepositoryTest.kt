package fr.plaglefleau.database.repositories.postgresql

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertEquals

class CardRepositoryTest: TestDatabaseBase() {

    private val repository = CardRepository()

    @Test
    fun `getCard by id returns card when it exists`() {
        // TODO: insert a card fixture first

        val result = repository.getCard(1)

        assertNotNull(result)
    }

    @Test
    fun `getCard by id returns null when card does not exist`() {
        val result = repository.getCard(999999)

        assertNull(result)
    }

    @Test
    fun `getCard by nfc returns card when it exists`() {
        // TODO: insert a card fixture with known NFC

        val result = repository.getCard("card-nfc")

        assertNotNull(result)
    }

    @Test
    fun `getCard by nfc returns null when card does not exist`() {
        val result = repository.getCard("missing-nfc")

        assertNull(result)
    }

    @Test
    fun `getBalance by id returns balance when card exists`() {
        // TODO: insert card with known balance

        val result = repository.getBalance(1)

        assertEquals(20.0, result)
    }

    @Test
    fun `getBalance by nfc returns balance when card exists`() {
        // TODO: insert card with known balance

        val result = repository.getBalance("card-nfc")

        assertEquals(20.0, result)
    }

    @Test
    fun `create inserts new card`() {
        // TODO: call create, then query it back

        repository.create(1234, "new-card-nfc")

        val result = repository.getCard("new-card-nfc")

        assertNotNull(result)
    }

    @Test
    fun `update by id changes pin and balance`() {
        // TODO: insert card fixture first

        repository.update(1, pin = 4321, amount = 50.0)

        val result = repository.getCard(1)

        assertNotNull(result)
        assertEquals(50.0, result.balance)
        assertEquals(4321, result.pin)
    }

    @Test
    fun `update by nfc changes pin and balance`() {
        // TODO: insert card fixture first

        repository.update("card-nfc", pin = 4321, amount = 50.0)

        val result = repository.getCard("card-nfc")

        assertNotNull(result)
        assertEquals(50.0, result.balance)
        assertEquals(4321, result.pin)
    }

    @Test
    fun `credit by id increases balance`() {
        // TODO: insert card fixture first

        repository.credit(1, 10.0)

        val result = repository.getBalance(1)

        assertNotNull(result)
        assertEquals(30.0, result)
    }

    @Test
    fun `credit by nfc increases balance`() {
        // TODO: insert card fixture first

        repository.credit("card-nfc", 10.0)

        val result = repository.getBalance("card-nfc")

        assertNotNull(result)
        assertEquals(30.0, result)
    }

    @Test
    fun `debit by id decreases balance`() {
        // TODO: insert card fixture first

        repository.debit(1, 5.0, "Stand 1")

        val result = repository.getBalance(1)

        assertNotNull(result)
        assertEquals(15.0, result)
    }

    @Test
    fun `debit by nfc decreases balance`() {
        // TODO: insert card fixture first

        repository.debit("card-nfc", 5.0, "Stand 1")

        val result = repository.getBalance("card-nfc")

        assertNotNull(result)
        assertEquals(15.0, result)
    }

    @Test
    fun `connect card by id and user id`() {
        // TODO: insert card + user fixtures

        repository.connect(1, 1)

        val result = repository.getCard(1)

        assertNotNull(result)
    }

    @Test
    fun `connect card by id and username`() {
        // TODO: insert card + user fixtures

        repository.connect(1, "user_1")

        val result = repository.getCard(1)

        assertNotNull(result)
    }

    @Test
    fun `connect card by nfc and user id`() {
        // TODO: insert card + user fixtures

        repository.connect("card-nfc", 1)

        val result = repository.getCard("card-nfc")

        assertNotNull(result)
    }

    @Test
    fun `connect card by nfc and username`() {
        // TODO: insert card + user fixtures

        repository.connect("card-nfc", "user_1")

        val result = repository.getCard("card-nfc")

        assertNotNull(result)
    }
}
