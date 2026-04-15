package fr.plaglefleau.api.validation

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class ValidationTestBase {

    @BeforeTest
    fun baseSetUp() {
        setupMocks()
    }

    @AfterTest
    fun baseTearDown() {
        tearDownMocks()
    }

    protected open fun setupMocks() {
        // override in child classes
    }

    protected open fun tearDownMocks() {
        // override in child classes if needed
    }
}
