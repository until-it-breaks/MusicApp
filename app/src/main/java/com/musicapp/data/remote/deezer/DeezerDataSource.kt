package com.musicapp.data.remote.deezer

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class DeezerDataSource(private val httpClient: HttpClient) {
    companion object {
        private const val BASE_URL = "https://api.deezer.com"
        private val json = Json {
            ignoreUnknownKeys = true
        }
    }

    suspend fun getTopPlaylists(limit: Int? = null): List<DeezerPlaylist> {
        val url = if (limit == null) "$BASE_URL/chart/0/playlists" else "$BASE_URL/chart/0/playlists?limit=${limit}"
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

    suspend fun getTopArtists(limit: Int? = null): List<DeezerArtist> {
        val url = if (limit == null) "$BASE_URL/chart/0/artists" else "$BASE_URL/chart/0/artists?limit=${limit}"
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

    suspend fun getTopAlbums(limit: Int? = null): List<DeezerAlbum> {
        val url = if (limit == null) "$BASE_URL/chart/0/albums" else "$BASE_URL/chart/0/albums?limit=${limit}"
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

    suspend fun getAlbumDetails(id: Long): DeezerAlbumDetails {
        val url = "$BASE_URL/album/${id}"
        val response: JsonObject = httpClient.get(url).body()
        return json.decodeFromJsonElement(response)
    }
}