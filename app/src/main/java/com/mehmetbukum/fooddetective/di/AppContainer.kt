package com.mehmetbukum.fooddetective.di

import android.content.Context
import com.mehmetbukum.fooddetective.FoodDetectiveViewModel
import com.mehmetbukum.fooddetective.data.AdditiveDataSource
import com.mehmetbukum.fooddetective.data.AdditiveRepository
import com.mehmetbukum.fooddetective.data.AdditiveSyncDataSource
import com.mehmetbukum.fooddetective.data.AppDatabase
import com.mehmetbukum.fooddetective.data.remote.AdditivesRemoteFactory

/**
 * Application-scoped dependency container.
 *
 * This keeps construction of Room, remote data source, repositories and ViewModels
 * out of Activity code without adding a full DI framework for the current app size.
 */
class AppContainer(
    context: Context,
    isDebuggable: Boolean
) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(appContext)
    }

    private val additiveRepository: AdditiveRepository by lazy {
        AdditiveRepository(
            dao = database.additiveDao(),
            remoteDataSource = AdditivesRemoteFactory.create(isDebuggable)
        )
    }

    val additiveDataSource: AdditiveDataSource by lazy {
        additiveRepository
    }

    val additiveSyncDataSource: AdditiveSyncDataSource by lazy {
        additiveRepository
    }

    fun createFoodDetectiveViewModel(): FoodDetectiveViewModel {
        return FoodDetectiveViewModel(
            repository = additiveDataSource,
            syncDataSource = additiveSyncDataSource
        )
    }
}
