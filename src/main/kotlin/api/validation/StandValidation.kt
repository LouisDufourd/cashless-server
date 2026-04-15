package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.IStandRepository

class StandValidation(private val standRepository: IStandRepository) {
    fun standExist(identifier: Any): Boolean {
        return when(identifier) {
            is Int -> standRepository.getStand(identifier) != null
            is String -> standRepository.getStand(identifier) != null
            else -> false
        }
    }
}