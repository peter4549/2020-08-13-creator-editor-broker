package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models

data class UserInformationModel(var categories: MutableList<Int>,
                                var publicName: String,
                                var uid: String,
                                var userType: Int,
                                var channelIds: MutableList<String> = mutableListOf(),
                                var commentsWritten: MutableList<CommentModel> = mutableListOf(),
                                var favoritePrIds: MutableList<String> = mutableListOf(),
                                var favoriteUserIds: MutableList<String> = mutableListOf(),
                                var myPrIds: MutableList<String> = mutableListOf(),
                                var partnerIds: MutableList<String> = mutableListOf(),
                                var profileImageUri: String? = null,
                                var pushToken: String? = null,
                                var receivedStarCount: Int = 0,
                                var registeredOnPartners: Boolean = false,
                                var tier: Int = Tier.NORMAL,
                                var userIdsReceivedMyStar: MutableList<String> = mutableListOf(),
                                var youtubeVideos: MutableList<VideoModel> = mutableListOf()) {
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

object UserType {
    const val CREATOR = 1
    const val EDITOR = 2
}

object CategoryNumber {
    const val CAR = 1
    const val BEAUTY_FASHION = 2
    const val COMEDY = 3
    const val EDUCATION = 4
    const val ENTERTAINMENT = 5
    const val FAMILY_ENTERTAINMENT = 6
    const val MOVIE_ANIMATION = 7
    const val FOOD = 8
    const val GAME = 9
    const val KNOW_HOW_STYLE = 10
    const val MUSIC = 11
    const val NEWS_POLITICS = 12
    const val NON_PROFIT_SOCIAL_MOVEMENT = 13
    const val PEOPLE_BLOG = 14
    const val PETS_ANIMALS = 15
    const val SCIENCE_TECHNOLOGY = 16
    const val SPORTS = 17
    const val TRAVEL_EVENT = 18
}