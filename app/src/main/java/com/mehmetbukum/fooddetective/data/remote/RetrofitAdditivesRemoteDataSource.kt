package com.mehmetbukum.fooddetective.data.remote

import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.data.AdditivesRemoteDataSource
import com.mehmetbukum.fooddetective.data.AdditivesVersionResponse

internal class RetrofitAdditivesRemoteDataSource(
    private val service: AdditivesRetrofitService
) : AdditivesRemoteDataSource {
    override suspend fun getAllAdditives(): List<Additive> {
        return service.getAllAdditives()
    }

    override suspend fun getVersion(): AdditivesVersionResponse {
        return service.getVersion()
    }

    override suspend fun getAdditivesSince(date: String): List<Additive> {
        return service.getAdditivesSince(date)
    }
}
