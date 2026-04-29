package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.StandDTO
import fr.plaglefleau.database.exceptions.*

/**
 * Defines persistence operations for reading stand data.
 *
 * Implementations provide lookup methods for retrieving stands either by their
 * internal numeric id or by their unique name.
 */
interface IStandRepository {

    /**
     * Retrieves a stand by its name.
     *
     * @param identifier the stand name to search for
     * @return the matching stand
     * @throws NotFoundException if no stand exists with this name
     */
    fun getStand(identifier: String): StandDTO

    /**
     * Retrieves a stand by its internal identifier.
     *
     * @param identifier the stand id to search for
     * @return the matching stand
     * @throws NotFoundException if no stand exists with this id
     */
    fun getStand(identifier: Int): StandDTO
}