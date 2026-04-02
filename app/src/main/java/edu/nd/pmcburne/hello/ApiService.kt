package edu.nd.pmcburne.hello
import retrofit2.http.GET

interface ApiService{
    @GET("~wxt4gm/placemarks.json")
    suspend fun getLocations(): List<LocationResponse>
}