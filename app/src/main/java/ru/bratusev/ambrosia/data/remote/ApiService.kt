package ru.bratusev.ambrosia.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.bratusev.ambrosia.data.model.Markers
import ru.bratusev.ambrosia.data.model.ResponseModel
import ru.bratusev.ambrosia.data.model.SendModel

interface ApiService {
    @GET("markers")
    suspend fun getAllMarks(): List<Markers>?

    @POST("markers/upload/")
    suspend fun uploadMarker(@Body sendModel: SendModel?): ResponseModel?
}

object RetrofitInstance {
    private const val BASE_URL = "http://tagproject-api.sfedu.ru/api/v1/map/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}