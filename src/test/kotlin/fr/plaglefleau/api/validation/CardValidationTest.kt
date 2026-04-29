package fr.plaglefleau.api.validation

import fr.plaglefleau.database.dto.CardDTO
import fr.plaglefleau.database.repositories.ICardRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.clearMocks
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardValidationTest : ValidationTestBase() {

    private val cardRepository: ICardRepository = mockk()

    override fun setupMocks() {
        clearMocks(cardRepository)
    }

    @Test
    fun verifyInvalidAmount() {
        val validation = CardValidation(cardRepository)

        assertTrue(validation.verifyInvalidAmount(-1.0))
        assertFalse(validation.verifyInvalidAmount(0.0))
        assertFalse(validation.verifyInvalidAmount(1.0))
    }

    @Test
    fun cardDontExist() {
        every { cardRepository.getCard(ValidationFixtures.CARD_ID) } returns null
        every { cardRepository.getCard(ValidationFixtures.CARD_NFC) } returns null

        val validation = CardValidation(cardRepository)

        assertFalse(validation.cardExist(ValidationFixtures.CARD_ID))
        assertFalse(validation.cardExist(ValidationFixtures.CARD_NFC))
    }

    @Test
    fun canDebitCard() {
        every { cardRepository.getBalance(ValidationFixtures.CARD_ID) } returns null
        every { cardRepository.getBalance(ValidationFixtures.CARD_NFC) } returns null

        val validation = CardValidation(cardRepository)

        assertFalse(validation.canDebitCard(ValidationFixtures.CARD_ID, 10.0))
        assertFalse(validation.canDebitCard(ValidationFixtures.CARD_NFC, 10.0))
    }

    @Test
    fun cardExist() {
        every { cardRepository.getCard(ValidationFixtures.CARD_ID) } returns CardDTO(
            id = ValidationFixtures.CARD_ID,
            nfc = ValidationFixtures.CARD_NFC,
            pin = 1234,
            balance = ValidationFixtures.CARD_BALANCE,
            userId = null
        )

        every { cardRepository.getCard(ValidationFixtures.CARD_NFC) } returns CardDTO(
            id = ValidationFixtures.CARD_ID,
            nfc = ValidationFixtures.CARD_NFC,
            pin = 1234,
            balance = ValidationFixtures.CARD_BALANCE,
            userId = null
        )

        val validation = CardValidation(cardRepository)
        assertTrue(validation.cardExist(ValidationFixtures.CARD_ID))
        assertTrue(validation.cardExist(ValidationFixtures.CARD_NFC))
    }
}
