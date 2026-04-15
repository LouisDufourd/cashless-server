package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.IStandRepository

/**
 * Provides validation helpers for stand-related business rules.
 *
 * This class is used to verify that a stand exists before performing operations
 * that depend on it.
 */
class StandValidation(private val standRepository: IStandRepository) {

    /**
     * Checks whether a stand exists for the given identifier.
     *
     * The identifier can be either an internal numeric id or a stand name.
     *
     * @param identifier the stand identifier to check
     * @return `true` if a matching stand exists, otherwise `false`
     */
    fun standExist(identifier: Any): Boolean {
        return when(identifier) {
            is Int -> standRepository.getStand(identifier) != null
            is String -> standRepository.getStand(identifier) != null
            else -> false
        }
    }
}