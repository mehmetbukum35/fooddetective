package com.mehmetbukum.fooddetective

import android.app.Application
import android.content.pm.ApplicationInfo
import com.mehmetbukum.fooddetective.di.AppContainer

class FoodDetectiveApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(
            context = this,
            isDebuggable = isDebuggableApp()
        )
    }

    private fun isDebuggableApp(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
