package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2

import android.content.Context
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChannelsItemsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistItemsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistsModel
import com.google.gson.internal.LinkedTreeMap
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class YouTubeDataApi(context: Context) {

    init {
        googleApiKey = context.getString(R.string.google_api_key)
    }

    fun getGoogleAuthorizationServerUrl() = "https://accounts.google.com/o/oauth2/auth?" +
            "client_id=$ANDROID_CLIENT_ID&" +
            "redirect_uri=$REDIRECT_URI&" +
            "scope=$SCOPE&" +
            "response_type=$RESPONSE_TYPE&" +
            "access_type=offline"

    interface AuthorizationService {
        @FormUrlEncoded
        @POST(GOOGLE_AUTHORIZATION_SERVER_URL)
        fun requestAuthorization(
            @Field("code") code: String,
            @Field("client_id") client_id: String = ANDROID_CLIENT_ID,
            @Field("redirect_uri") redirect_uri: String = REDIRECT_URI,
            @Field("grant_type") grant_type: String = "authorization_code"
        ): Call<LinkedTreeMap<String, Any>>
    }

    interface ChannelsService {
        @GET("/youtube/v3/channels")
        fun requestByAccessToken(
            @Header("Authorization") Authorization: String,
            @Query("part") part: String = "id,snippet",
            @Query("mine") mine: Boolean = true
        ): Call<ChannelsItemsModel>

        @GET("/youtube/v3/channels")
        fun requestById(
            @Query("key") key: String = googleApiKey,
            @Query("part") part: String = "snippet",
            @Query("id") id: String
        ): Call<ChannelsItemsModel>
    }

    interface PlaylistsService {
        @GET("/youtube/v3/playlists")
        fun requestByChannelId(
            @Query("key") key: String = googleApiKey,
            @Query("part") part: String = "snippet",
            @Query("channelId") channelId: String,
            @Query("maxResults") maxResults: Int = 10
        ): Call<PlaylistsModel>
    }

    interface PlaylistItemsService {
        @GET("/youtube/v3/playlistItems")
        fun requestById(
            @Query("key") key: String = googleApiKey,
            @Query("part") part: String = "contentDetails",
            @Query("playlistId") playlistId: String
        ): Call<PlaylistItemsModel>
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getAuthorizationService(): AuthorizationService =
        retrofit.create(AuthorizationService::class.java)

    fun getChannelsService(): ChannelsService =
        retrofit.create(ChannelsService::class.java)

    fun getPlaylistsService(): PlaylistsService =
        retrofit.create(PlaylistsService::class.java)


    /*
    object GoogleApisRequest {
        private val retrofit =
            Retrofit.Builder().baseUrl("https://www.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        fun getPlaylistItemsService(): YouTubeDataApis.PlaylistItemsService =
            retrofit.create(YouTubeDataApis.PlaylistItemsService::class.java)

        fun getVideosService(): YouTubeDataApis.VideosService =
            retrofit.create(YouTubeDataApis.VideosService::class.java)
    }

     */

    // Authorization
    /*
    private fun getAuthorizationFormBody(code: String) =
        FormBody.Builder().add("code", code)
            .add("client_id", ANDROID_CLIENT_ID)
            .add("redirect_uri", REDIRECT_URI)
            .add("grant_type", "authorization_code").build()

    fun getAuthorizationRequest(code: String) = Request.Builder()
        .header("Content-Type", "application/x-www-form-urlencoded")
        .url(GOOGLE_AUTHORIZATION_SERVER_URL)
        .post(getAuthorizationFormBody(code))
        .build()

    // Channels
    fun getChannelFromResponse(response: Response): ChannelModel {
        val map: Map<*, *>? =
            Gson().fromJson(response.body?.string(), Map::class.java)
        val item = (map?.get("items") as ArrayList<*>)[0] as LinkedTreeMap<*, *>
        val id = item["id"] as String
        val snippet = item["snippet"] as LinkedTreeMap<*, *>
        val title = snippet["title"] as String
        val description = snippet["description"] as String
        val thumbnailUri = ((snippet["thumbnails"]
                as LinkedTreeMap<*, *>)["default"]
                as LinkedTreeMap<*, *>)["url"] as String
        return ChannelModel(id, description, thumbnailUri, title)
    }

    fun getChannelIdsRequestByAccessToken(accessToken: String) =
        Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $accessToken")
            .url(CHANNELS_REQUEST_URL)
            .get()
            .build()

    fun getChannelRequestById(channelId: String): Request {
        val url = "https://www.googleapis.com/youtube/v3/channels?key=$googleApiKey&" +
                "part=snippet,statistics&id=$channelId"
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getChannelsRequestByIds(channelIds: MutableList<String>): Request {
        val url =
            "https://www.googleapis.com/youtube/v3/channels?key=$googleApiKey&" +
                    "part=snippet,statistics&id=${channelIds.joinToString(separator = ",")}" // for make error
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getChannelsFromResponse(response: Response): MutableList<ChannelModel> {
        val map = Gson().fromJson(response.body?.string(), Map::class.java)
        val items = map["items"] as ArrayList<*>?
        val channels = mutableListOf<ChannelModel>()

        for (item in items!!) {
            val id = (item as LinkedTreeMap<*, *>)["id"] as String
            val snippet = item["snippet"] as LinkedTreeMap<*, *>
            val title = snippet["title"] as String
            val description = snippet["description"] as String
            val thumbnailUri = ((snippet["thumbnails"]
                    as LinkedTreeMap<*, *>)["default"]
                    as LinkedTreeMap<*, *>)["url"] as String
            channels.add(ChannelModel(id, description, thumbnailUri, title))
        }

        return channels
    }

    // Playlists
    fun getPlaylistsRequestByChannelId(channelId: String): Request {
        val url = "https://www.googleapis.com/youtube/v3/playlists?key=$googleApiKey&" +
                "part=snippet&channelId=$channelId"
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getPlaylistsFromRequest(response: Response): MutableList<PlaylistModel> {
        val map = Gson().fromJson(response.body?.string(), Map::class.java)
        val items = map["items"] as ArrayList<*>?
        val playlists = mutableListOf<PlaylistModel>()

        for (item in items!!) {
            val id = (item as LinkedTreeMap<*, *>)["id"] as String
            val snippet = item["snippet"] as LinkedTreeMap<*, *>?
            val title = snippet?.get("title") as String
            val description = snippet["description"] as String
            val thumbnailUri = ((snippet["thumbnails"]
                    as LinkedTreeMap<*, *>)["default"]
                    as LinkedTreeMap<*, *>)["url"] as String
            playlists.add(PlaylistModel(description, id, thumbnailUri, title))
        }

        return playlists
    }

    // Videos
    fun getVideosRequestByChannelId(channelId: String): Request {
        val url = "https://www.googleapis.com/youtube/v3/search?key=$googleApiKey&" +
                "part=snippet&channelId=$channelId"
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getVideosRequestByPlaylistId(playlistId: String): Request {
        val url = "https://www.googleapis.com/youtube/v3/playlistItems?key=$googleApiKey&" +
                "part=contentDetails&playlistId=$playlistId"
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getVideosRequestByIds(videoIds: String): Request {
        val url = "https://www.googleapis.com/youtube/v3/videos?key=$googleApiKey&" +
                "part=snippet,statistics&id=$videoIds"
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getVideoIdsFromResponse(response: Response, isAllVideos: Boolean): List<String> {
        val map: Map<*, *>? =
            Gson().fromJson(response.body?.string(), Map::class.java)
        val items = (map?.get("items") as ArrayList<*>)

        return if (isAllVideos)
            items.filter { ((it as LinkedTreeMap<*, *>)["id"] as LinkedTreeMap<*, *>)["kind"] == "youtube#video" }
                .map { ((it as LinkedTreeMap<*, *>)["id"] as LinkedTreeMap<*, *>)["videoId"] as String }
        else
            items.map {
                ((it as LinkedTreeMap<*, *>)["contentDetails"]
                        as LinkedTreeMap<*, *>)["videoId"] as String
            }
    }

    fun getVideosFromResponse(response: Response): MutableList<VideoModel> {
        val map: Map<*, *>? =
            Gson().fromJson(response.body?.string(), Map::class.java)
        val items = map?.get("items") as ArrayList<*>
        val videos = mutableListOf<VideoModel>()

        for (item in items) {
            val id = (item as LinkedTreeMap<*, *>)["id"] as String
            val snippet = item["snippet"] as LinkedTreeMap<*, *>
            val channelId = snippet["channelId"] as String
            val description = snippet["description"] as String
            val publishTime = snippet["publishedAt"] as String
            val title = snippet["title"] as String
            val thumbnails = snippet["thumbnails"] as LinkedTreeMap<*, *>
            val thumbnailUri = (thumbnails["default"] as LinkedTreeMap<*, *>)["url"] as String

            videos.add(0, VideoModel(channelId, description, publishTime, thumbnailUri, title, id))
        }

        return videos
    }

     */

    companion object {
        private const val GOOGLE_AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/token"
        private const val ANDROID_CLIENT_ID = "39537376089-vjs4br85vjkm2mf7lacb3fu7j98r1aii.apps.googleusercontent.com"
        private const val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
        private const val RESPONSE_TYPE = "code"
        private const val SCOPE = "https://www.googleapis.com/auth/youtube"
        // private const val YOUTUBE_OAUTH_SCOPE = "oauth2:https://www.googleapis.com/auth/youtube"
        private lateinit var androidClientId: String
        private lateinit var googleApiKey: String
    }
}