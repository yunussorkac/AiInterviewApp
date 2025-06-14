package com.yeslab.interviewapp.data.remote

import com.yeslab.interviewapp.model.GptRequest
import com.yeslab.interviewapp.model.GptResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GptApiService {

    @POST("chat/completions")
    suspend fun generateQuestions(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: GptRequest
    ): Response<GptResponse>

}