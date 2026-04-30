package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class TestDatabaseBase {

    @BeforeTest
    fun setUpDatabase() {
        DatabaseFactory.init(
            database = "cashless-test",
            user = "cashless_user-test",
            password = "cashless_password-test",
            serverNames = arrayOf("localhost"),
            portNumber = intArrayOf(4062)
        )

        DatabaseFactory.executeSqlFile("docker/db/test/11-testDataGeneration.sql")
    }
}
