package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class TestDatabaseBase {

    @BeforeTest
    fun setUpDatabase() {
        // TODO: initialize the test database connection here
        // Example:
        // DatabaseFactory.init(
        //     url = "jdbc:postgresql://localhost:9876/cashless",
        //     user = "cashless_user",
        //     password = "cashless_password"
        // )
        //
        // TODO: run schema/init script or ensure the DB is already initialized
        DatabaseFactory.init(
            database = "cashless-test",
            user = "cashless_user-test",
            password = "cashless_password-test",
            serverNames = arrayOf("localhost"),
            portNumber = intArrayOf(4062)
        )

        DatabaseFactory.executeSqlFile("docker/db/testDataGeneration.sql")
    }
}
