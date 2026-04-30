package fr.plaglefleau.api.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardValidationTest : ValidationTestBase() {

    @Test
    fun verifyInvalidAmount() {
        assertTrue(CardValidation.verifyInvalidAmount(-1.0))
        assertFalse(CardValidation.verifyInvalidAmount(0.0))
        assertFalse(CardValidation.verifyInvalidAmount(1.0))
    }
}
