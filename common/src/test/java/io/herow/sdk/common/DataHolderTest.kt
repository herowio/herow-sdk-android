package io.herow.sdk.common

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class DataHolderTest {
    private lateinit var dataHolder: DataHolder

    @Before
    fun setUp() {
        dataHolder = DataHolder(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testSet() {
        val defaultValue = "Unit Tests"
        val newValue = "New value"
        Assert.assertEquals(defaultValue, dataHolder["test", defaultValue])
        dataHolder["test"] = newValue
        Assert.assertNotEquals(defaultValue, dataHolder["test", defaultValue])
        Assert.assertEquals(newValue, dataHolder.get<String>("test"))
    }
}