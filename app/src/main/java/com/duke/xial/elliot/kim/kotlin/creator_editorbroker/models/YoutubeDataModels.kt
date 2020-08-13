@file:Suppress("unused", "SpellCheckingInspection")

package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models

data class PlaylistItemsModel(val kind: String,
                              val etag: String,
                              val items: List<ItemModel>)

data class VideosItemsModel(val nextPageToken: String,
                            val prevPageToken: String,
                            val items: List<ItemModel>)

data class ItemModel(val kind: String,
                     val etag: String,
                     val id: String,
                     val contentDetails: ContentDetailsModel,
                     val snippet: SnippetModel,
                     val statistics: StandardModel)

data class ContentDetailsModel(val videoId: String,
                               val videoPublishedAt: String)

data class SnippetModel(val publishedAt: String,
                        val channelId: String,
                        val title: String,
                        val description: String,
                        val thumbnails: ThumbnailsModel)

data class ThumbnailsModel(val default: DefaultModel,
                           val standard: StandardModel)

data class DefaultModel(val url: String)

data class StandardModel(val url: String)

data class StatisticsModel(val viewCount: String,
                           val likeCount: String,
                           val dislikeCount: String,
                           val favoriteCount: String,
                           val commentCount: String)