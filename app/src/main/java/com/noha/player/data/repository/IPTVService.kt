package com.noha.player.data.repository

import retrofit2.http.GET
import retrofit2.http.Url

interface IPTVService {
    @GET
    suspend fun fetchPlaylist(@Url url: String): String
}

