@file:Suppress("unused", "SpellCheckingInspection")

package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models

import com.google.common.primitives.UnsignedInteger

data class ChannelsItemsModel(val items: List<ItemModel>)

data class PlaylistsModel(val kind: String,
                          val etag: String,
                          val nextPageToken: String,
                          val items: List<ItemModel>)

data class PlaylistItemsModel(val kind: String,
                              val etag: String,
                              val nextPageToken: String,
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

data class ContentDetailsModel(val itemCount: Int,
                               val videoId: String,
                               val videoPublishedAt: String)

data class SnippetModel(val publishedAt: String,
                        val channelId: String,
                        val title: String,
                        val description: String,
                        val thumbnails: ThumbnailsModel)

data class ThumbnailsModel(val default: DefaultModel,
                           val medium: MediumModel,
                           val standard: StandardModel?)

data class DefaultModel(val url: String)

data class MediumModel(val url: String)

// Playlists, Videos
data class StandardModel(val url: String)

data class StatisticsModel(val viewCount: String,
                           val likeCount: String,
                           val dislikeCount: String,
                           val favoriteCount: String,
                           val commentCount: String)