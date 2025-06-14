package com.yeslab.interviewapp.model

data class GptRequest(
    val model: String = "gpt-4.1-nano",
    val messages: List<GptMessage>,
    val max_tokens: Int = 1000,
    val temperature: Double = 0.7
)

data class GptMessage(
    val role: String,
    val content: String
)

data class GptResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<GptChoice>
)

data class GptChoice(
    val index: Int,
    val message: GptMessage,
    val finish_reason: String
)

data class GptError(
    val error: GptErrorDetail
)

data class GptErrorDetail(
    val message: String,
    val type: String,
    val param: String?,
    val code: String?
)
