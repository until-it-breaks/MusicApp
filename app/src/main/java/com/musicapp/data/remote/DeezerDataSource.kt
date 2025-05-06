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
    @SerialName("picture_medium")
    val mediumPicture: String
)

@Serializable
data class DeezerArtist(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("picture_medium")
    val mediumPicture: String,
    @SerialName("position")
    val position: Int
)

@Serializable
data class DeezerAlbum(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("cover_medium")
    val mediumCover: String,
    @SerialName("explicit_lyrics")
    val explicit: Boolean
)

class DeezerDataSource(private val httpClient: HttpClient) {
    companion object {
        private const val BASE_URL = "https://api.deezer.com"
        private val json = Json {
            ignoreUnknownKeys = true
        }
    }

    suspend fun getTopPlaylists(): List<DeezerPlaylist> {
        val url = "$BASE_URL/chart/0/playlists"
        val responseBody: JsonObject = httpClient.get(url).body()
        val dataElement = responseBody["data"]
        return if (dataElement != null) {
            json.decodeFromJsonElement(
                ListSerializer(DeezerPlaylist.serializer()),
                dataElement
            )
        } else {
            emptyList()
        }
    }

    suspend fun getTopArtists(): List<DeezerArtist> {
        val url = "$BASE_URL/chart/0/artists"
        val responseBody: JsonObject = httpClient.get(url).body()
        val dataElement = responseBody["data"]
        return if (dataElement != null) {
            json.decodeFromJsonElement(
                ListSerializer(DeezerArtist.serializer()),
                dataElement
            )
        } else {
            emptyList()
        }
    }

    suspend fun getTopAlbums(): List<DeezerAlbum> {
        val url = "$BASE_URL/chart/0/albums"
        val responseBody: JsonObject = httpClient.get(url).body()
        val dataElement = responseBody["data"]
        return if (dataElement != null) {
            json.decodeFromJsonElement(
                ListSerializer(DeezerAlbum.serializer()),
                dataElement
            )
        } else {
            emptyList()
        }
    }
}