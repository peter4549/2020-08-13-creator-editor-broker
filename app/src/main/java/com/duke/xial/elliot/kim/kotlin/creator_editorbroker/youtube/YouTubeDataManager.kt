package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoModel

class YouTubeDataManager private constructor() {
    // Key: Channel ID
    val playlistsInUse: MutableMap<String, ArrayList<PlaylistDataModel>> = mutableMapOf()
    val nextPageTokensOfPlaylists: MutableMap<String, String?> = mutableMapOf()

    // Key: Playlist ID
    val videosInUse: MutableMap<String, ArrayList<VideoModel>> = mutableMapOf()
    val nextPageTokenOfVideos: MutableMap<String, String?> = mutableMapOf()

    fun registerNextPageTokenAndPlaylists(channelId: String, nextPageToken: String?, playlists: ArrayList<PlaylistDataModel>) {
        nextPageTokensOfPlaylists[channelId] = nextPageToken
        playlistsInUse[channelId] = playlists
    }

    fun registerNextPageTokenAndVideos(playlistId: String, nextPageToken: String?, videos: ArrayList<VideoModel>) {
        nextPageTokenOfVideos[playlistId] = nextPageToken
        videosInUse[playlistId] = videos
    }

    companion object {
        private val youTubeDataManager: YouTubeDataManager? = null
        fun getInstance(): YouTubeDataManager = youTubeDataManager ?: YouTubeDataManager()
    }
}