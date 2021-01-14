package io.herow.sdk.common

import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.states.DataHolder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class DataHolderTest {
    @Before
    fun setUp() {
        DataHolder.init(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testSet() {
        val defaultValue = "Unit Tests"
        val newValue = "New value"
        Assert.assertEquals(defaultValue, DataHolder["test", defaultValue])
        DataHolder["test"] = newValue
        Assert.assertNotEquals(defaultValue, DataHolder["test", defaultValue])
        Assert.assertEquals(newValue, DataHolder.get<String>("test"))
    }
}