package com.fvk_solutions.fvkstock
import com.fvk_solutions.fvkstock.models.LoginApiResponse
import com.fvk_solutions.fvkstock.models.LoginRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/Authorization/Login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginApiResponse>

    @GET("/api/Authorization/Secret")
    suspend fun secret(): Response<String>

}
