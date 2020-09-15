package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

data class YouTubeErrorModels(val error: ErrorModel)

data class ErrorModel(val code: Double?,
                      val message: String?,
                      val errors: List<InnerErrorModel>?,
                      val status: String?) {
    fun getReason() = errors?.get(0)?.reason
}

data class InnerErrorModel(val message: String?,
                           val domain: String?,
                           val reason: String?)