package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler

data class ErrorBodyModel(val error: ErrorModel)

data class ErrorModel(val code: Double?,
                      val message: String?,
                      val errors: List<InnerErrorModel>?,
                      val status: String?)

data class InnerErrorModel(val message: String?,
                           val domain: String?,
                           val reason: String?)