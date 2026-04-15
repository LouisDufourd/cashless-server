package fr.plaglefleau.api.validation
import fr.plaglefleau.database.repositories.IStandRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StandValidationTest : ValidationTestBase() {

    private val standRepository: IStandRepository = mockk()

    override fun setupMocks() {
        clearMocks(standRepository)
    }

    @Test
    fun standDontExist() {
        every { standRepository.getStand(ValidationFixtures.STAND_ID) } returns null
        every { standRepository.getStand(ValidationFixtures.STAND_NAME) } returns null

        val validation = StandValidation(standRepository)

        assertFalse(validation.standExist(ValidationFixtures.STAND_ID))
        assertFalse(validation.standExist(ValidationFixtures.STAND_NAME))
    }

    @Test
    fun standExist() {
        every { standRepository.getStand(ValidationFixtures.STAND_ID) } returns mockk(relaxed = true)
        every { standRepository.getStand(ValidationFixtures.STAND_NAME) } returns mockk(relaxed = true)

        val validation = StandValidation(standRepository)
        assertTrue(validation.standExist(ValidationFixtures.STAND_ID))
        assertTrue(validation.standExist(ValidationFixtures.STAND_NAME))
    }
}
