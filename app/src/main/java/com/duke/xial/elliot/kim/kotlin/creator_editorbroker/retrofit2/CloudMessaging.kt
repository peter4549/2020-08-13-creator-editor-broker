package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2

import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging.CloudMessageModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging.SENDER_ID
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging.SERVER_KEY
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

object CloudMessaging {

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    interface CloudMessagingService {
        @POST("send")
        fun request(
            @HeaderMap headers: Map<String, String> = mapOf(
                "Authorization" to "key=$SERVER_KEY",
                "project_id" to SENDER_ID
            ),
            @Body requestBody: CloudMessageModel
        ): Call<ResponseBody>
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fcm.googleapis.com/fcm/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun getCloudMessagingService(): CloudMessagingService =
        retrofit.create(CloudMessagingService::class.java)
}

/*
.header("Content-Type", "application/json")
.addHeader("Authorization", API_KEY)
// post sample
interface AuthorizationService {
    @FormUrlEncoded
    @POST(YouTubeDataApi.GOOGLE_AUTHORIZATION_SERVER_URL)
    fun requestAuthorization(
        @Field("code") code: String,
        @Field("client_id") client_id: String = YouTubeDataApi.ANDROID_CLIENT_ID,
        @Field("redirect_uri") redirect_uri: String = YouTubeDataApi.REDIRECT_URI,
        @Field("grant_type") grant_type: String = "authorization_code"
    ): Call<LinkedTreeMap<String, Any>>
}


// 바로 밑에가 공식인ㄷㅣ? 하울 이쉐키 잘하네!
{
    "message":{
    "token":"bk3RNwTe3H0:CI2k_HHwgIpoDKCIZvvDMExUdFQ3P1...",
    "notification":{
    "title":"Portugal vs. Denmark",
    "body":"great match!"
},
    "data" : {
    "Nick" : "Mario",
    "Room" : "PortugalVSDenmark"
}
}
}
// 밑에꺼로 request 구성할 것.
{
    "registration_ids":["dOjncxmaRgudirk4eECLn-:APA91bEuqxFOKaqIsklPt2o8v2UjJqjq70MmdcmdHyTFE9iO_aunNjLxOFiz92-9g0fN7D40TAlgpalwgHStQmUnnlcHVUy-lou-mLQGzXdJZ6nHEn2zrYiH0y4uafUsEkO4LdnY46SN","dFIrtXxdSOSa4W-P_o7ueb:APA91bHhh6e6PFYCYvPsstrut2fxDcpo67Y9evjhgu3Fj6VUgvJjY372R9P4biXcD22tc9Lls5p0eZcqAm8Lck25eUuXGgKZZN-dzOyBYLFHBKWV9ugY_2JUAsFlPm1iNkp7Vq4gp8Qx"],
    "notification":{
    "title":"Portugal vs. Denmark",
    "body":"great match!"
}

    // result body
    {
        "multicast_id": 7541459383489952020,
        "success": 0,
        "failure": 1,
        "canonical_ids": 0,
        "results": [
        {
            "error": "InvalidRegistration"
        }
        ]
    }
}
val url = "https://fcm.googleapis.com/fcm/send"
val cloudMessage = CloudMessageModel()

cloudMessage.to = pushToken

// cloudMessage.notification.click_action = ACTION_MAIN
cloudMessage.notification.title = currentUserPublicName
cloudMessage.notification.text = edit_text_message.text.toString()

cloudMessage.data.message = edit_text_message.text.toString()
cloudMessage.data.roomId = chatRoom!!.roomId
cloudMessage.data.senderPublicName = currentUserPublicName

val requestBody =
    RequestBody.create(MediaType.parse("application/json; charset=utf8"),
        Gson().toJson(cloudMessage))
val request = Request.Builder().header("Content-Type", "application/json")
    .addHeader("Authorization", API_KEY)
    .url(url)
    .post(requestBody)
    .build()

 */