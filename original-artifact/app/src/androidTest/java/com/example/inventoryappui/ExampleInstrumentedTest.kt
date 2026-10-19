package com.example.inventoryappui

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

//Instrumented test

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Get the app context
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Verify the correct package name
        assertEquals("com.example.inventoryappui", appContext.packageName)
    }
}