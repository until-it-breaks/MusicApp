package com.musicapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
data class DeezerPlaylist(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("picture_small")
    val pictureSmall: String
)

class DeezerDataSource(private val httpClient: HttpClient) {
    companion object {
        private const val BASE_URL = "https://api.deezer.com"
    }

    suspend fun getTopPlaylists(): List<DeezerPlaylist> {
        val url = "$BASE_URL/chart/0/playlists"
        val responseBody: JsonObject = httpClient.get(url).body()

        val json = Json {
            ignoreUnknownKeys = true
        }

        val dataElement = responseBody["data"]
        return if (dataElement != null) {
            json.decodeFromJsonElement(
                ListSerializer(DeezerPlaylist.serializer()),
                dataElement
            )
        } else {
            emptyList() // Or handle the error appropriately
        }
    }
}