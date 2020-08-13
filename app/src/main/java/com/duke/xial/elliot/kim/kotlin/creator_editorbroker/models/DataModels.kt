package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models

data class UserInformationModel(val categories: MutableList<String?>,
                                val publicName: String,
                                val uid: String,
                                val userType: String,
                                val channelIds: MutableList<String> = mutableListOf(),
                                val commentsWritten: MutableList<CommentModel> = mutableListOf(),
                                val favoritePrIds: MutableList<String> = mutableListOf(),
                                val favoriteUserIds: MutableList<String> = mutableListOf(),
                                val myPrIds: MutableList<String> = mutableListOf(),
                                val partnerIds: MutableList<String> = mutableListOf(),
                                val profileImageUri: String? = null,
                                val pushToken: String? = null,
                                val receivedStarCount: Int = 0,
                                val registeredOnPartners: Boolean = false,
                                val tier: Int = Tier.NORMAL,
                                val userIdsReceivedMyStar: MutableList<String> = mutableListOf(),
                                val youtubeVideos: MutableList<VideoModel>) {
    companion object {
        const val KEY_CATEGORIES = "categories"
        const val KEY_PROFILE_IMAGE_URI = "profileImageUri"
        const val KEY_PUBLIC_NAME = "publicName"
        const val KEY_UID = "uid"
        const val KEY_USER_TYPE = "userType"
        const val KEY_CHANNEL_IDS = "channelIds"
        const val KEY_COMMENTS_WRITTEN = "commentsWritten"
        const val KEY_FAVORITE_PR_IDS = "favoritePrIds"
        const val KEY_FAVORITE_USER_IDS = "favoriteUserIds"
        const val KEY_MY_PR_IDS = "myPrIds"
        const val KEY_PARTNER_IDS = "partnerIds"
        const val KEY_PUSH_TOKEN = "pushToken"
        const val KEY_RECEIVED_STAR_COUNT = "receivedStarCount"
        const val KEY_REGISTERED_ON_PARTNERS = "registeredOnPartners"
        const val KEY_TIER = "tier"
        const val KEY_USER_IDS_RECEIVED_MY_STAR = "userIdsReceivedMyStar"
        const val KEY_YOUTUBE_VIDEOS = "youtubeVideos"
    }
}

data class CommentModel (
    val comment: String
)

data class PrModel(var categories: MutableList<String?>,
                   var description: String,
                   var userType: String,
                   var publisherId: String,
                   var publisherPublicName: String,
                   var registrationTime: String,
                   var title: String,
                   var youtubeVideos: MutableList<VideoModel> = mutableListOf())

data class VideoModel(val id: String,
                      val snippet: SnippetModel)

object Tier {
    const val NORMAL = 0
    const val PREMIUM = 1
}