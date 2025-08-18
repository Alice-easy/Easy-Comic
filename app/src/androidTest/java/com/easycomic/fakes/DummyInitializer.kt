package com.easycomic.fakes

import android.content.Context
import androidx.startup.Initializer

/**
 * A dummy initializer to ensure that the androidx.startup.InitializationProvider
 * is correctly merged into the test manifest. This helps resolve ClassNotFoundExceptions
 * for the provider in instrumented tests.
 */
class DummyInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // This initializer does nothing. Its purpose is purely for manifest merging.
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}