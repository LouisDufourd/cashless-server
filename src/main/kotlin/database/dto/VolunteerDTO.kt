package fr.plaglefleau.database.dto

data class VolunteerDTO(
    val id: Int,
    val username: String,
    val password: String,
    val role: RoleDTO,
    val civility: CivilityDTO,
    val stand: StandDTO
)