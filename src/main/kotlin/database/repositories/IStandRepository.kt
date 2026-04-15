package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.StandDTO

interface IStandRepository {
    fun getStand(identifier: String): StandDTO?
    fun getStand(identifier: Int): StandDTO?
}