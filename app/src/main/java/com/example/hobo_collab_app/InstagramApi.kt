package com.example.hobo_collab_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface InstagramApi {
    @GET("oauth/authorize")

    fun getAuthorizationUrl(
        @Query("client_id") clientId: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("response_type") responseType: String,
        @Query("scope") scope: String
    ): Call<String>
}