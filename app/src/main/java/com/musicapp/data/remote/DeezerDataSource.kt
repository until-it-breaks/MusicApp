package com.musicapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName

data class DeezerPlaylist(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("cover")
    val cover: ContentType.Image
)

class DeezerDataSource(private val httpClient: HttpClient) {
    companion object {
        private const val BASE_URL = "https://api.deezer.com"
    }

    suspend fun getTopPlaylists(): List<DeezerPlaylist> {
        val url = "$BASE_URL/chart/0/albums"
        return httpClient.get(url).body()
    }
}