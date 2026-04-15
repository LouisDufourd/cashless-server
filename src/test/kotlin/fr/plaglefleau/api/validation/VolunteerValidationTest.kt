package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.IVolunteerRepository
import fr.plaglefleau.database.tables.RoleName
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VolunteerValidationTest : ValidationTestBase() {

    private val volunteerRepository: IVolunteerRepository = mockk()

    override fun setupMocks() {
        clearMocks(volunteerRepository)
    }

    @Test
    fun volunteerHasRoleNull() {
        every { volunteerRepository.getRole(ValidationFixtures.VOLUNTEER_ID) } returns null

        val validation = VolunteerValidation(volunteerRepository)

        assertFalse(validation.volunteerHasRole(ValidationFixtures.VOLUNTEER_ID, RoleName.ORGANIZER))
    }

    @Test
    fun volunteerHasRole() {
        every { volunteerRepository.getRole(ValidationFixtures.VOLUNTEER_ID) } returns RoleName.ORGANIZER

        val validation = VolunteerValidation(volunteerRepository)

        assertTrue(validation.volunteerHasRole(ValidationFixtures.VOLUNTEER_ID, RoleName.ORGANIZER))
        assertTrue(validation.volunteerHasRole(ValidationFixtures.VOLUNTEER_ID, RoleName.MANAGER, RoleName.ORGANIZER))
        assertFalse(validation.volunteerHasRole(ValidationFixtures.VOLUNTEER_ID, RoleName.RECHARGE))
        assertFalse(validation.volunteerHasRole(ValidationFixtures.VOLUNTEER_ID, RoleName.RECHARGE, RoleName.MANAGER))
    }
}
