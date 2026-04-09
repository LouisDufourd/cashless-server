package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.StandRepository

object StandValidation {
    fun standExist(identifier: Any): Boolean {
        return when(identifier) {
            is Int -> StandRepository().getStand(identifier) != null
            is String -> StandRepository().getStand(identifier) != null
            else -> false
        }
    }
}