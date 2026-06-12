package com.mehmetbukum.fooddetective.data.remote

import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.data.AdditivesVersionResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface AdditivesRetrofitService {
    @GET("api/additives")
    suspend fun getAllAdditives(): List<Additive>

    @GET("api/additives/version")
    suspend fun getVersion(): AdditivesVersionResponse

    @GET("api/additives/since")
    suspend fun getAdditivesSince(@Query("date") date: String): List<Additive>
}
