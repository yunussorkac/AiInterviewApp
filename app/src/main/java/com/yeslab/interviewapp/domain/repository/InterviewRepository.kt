package com.yeslab.interviewapp.domain.repository

import com.google.gson.Gson
import com.yeslab.interviewapp.data.local.InterviewDao
import com.yeslab.interviewapp.data.local.InterviewQuestionEntity
import com.yeslab.interviewapp.data.local.InterviewRecordEntity
import com.yeslab.interviewapp.data.remote.GptApiService
import com.yeslab.interviewapp.model.DifficultyLevel
import com.yeslab.interviewapp.model.GptMessage
import com.yeslab.interviewapp.model.GptRequest
import com.yeslab.interviewapp.model.InterviewConfig
import com.yeslab.interviewapp.model.InterviewEvaluation
import com.yeslab.interviewapp.model.Language
import com.yeslab.interviewapp.model.QuestionAnswer
import java.util.Date

class InterviewRepository (
    private val apiService: GptApiService,
    private val interviewDao: InterviewDao
) {

    companion object {
        private const val API_KEY = ""
    }

    suspend fun saveInterviewRecord(
        config: InterviewConfig,
        questionsAndAnswers: List<QuestionAnswer>,
        evaluation: InterviewEvaluation
    ) {
        val record = InterviewRecordEntity(
            date = Date(),
            profession = config.profession,
            difficulty = config.difficulty.label,
            language = config.language.label,
            overallScore = evaluation.overallScore,
            strengths = Gson().toJson(evaluation.strengths),
            weaknesses = Gson().toJson(evaluation.weaknesses),
            recommendations = Gson().toJson(evaluation.recommendations),
            detailedFeedback = evaluation.detailedFeedback
        )

        val interviewId = interviewDao.insertInterviewRecord(record)

        val questionEntities = questionsAndAnswers.mapIndexed { index, qa ->
            InterviewQuestionEntity(
                interviewId = interviewId,
                questionNumber = index + 1,
                question = qa.question,
                answer = qa.answer
            )
        }

        interviewDao.insertInterviewQuestions(questionEntities)
    }






    suspend fun generateQuestions(config: InterviewConfig): Result<List<String>> {
        return try {
            val prompt = createPrompt(config)
            val request = GptRequest(
                messages = listOf(
                    GptMessage("user", prompt)
                ),
                max_tokens = 1500,
                temperature = 0.7
            )

            val response = apiService.generateQuestions(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.let { gptResponse ->
                    val questionsText = gptResponse.choices.firstOrNull()?.message?.content
                    val questions = parseQuestions(questionsText)
                    Result.success(questions)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createPrompt(config: InterviewConfig): String {
        val languageText = when (config.language) {
            Language.ENGLISH -> "English"
            Language.TURKISH -> "Turkish"
        }

        val difficultyText = when (config.difficulty) {
            DifficultyLevel.BASIC -> "basic level"
            DifficultyLevel.INTERMEDIATE -> "intermediate level"
            DifficultyLevel.ADVANCED -> "advanced level"
        }

        return """
            Generate ${config.questionCount} interview questions for a ${config.profession} position.
            
            Requirements:
            - Difficulty level: $difficultyText
            - Language: $languageText
            - Questions should be professional and relevant to the profession
            - Each question should be numbered (1., 2., 3., etc.)
            - Questions should test both technical skills and soft skills
            - Make questions challenging but fair for the specified difficulty level
            
            Please provide only the questions, numbered from 1 to ${config.questionCount}.
        """.trimIndent()
    }

    private fun parseQuestions(questionsText: String?): List<String> {
        if (questionsText.isNullOrBlank()) return emptyList()

        return questionsText
            .split("\n")
            .filter { it.trim().isNotEmpty() }
            .mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.matches(Regex("^\\d+\\..*"))) {
                    trimmed.substringAfter(".").trim()
                } else if (trimmed.isNotEmpty()) {
                    trimmed
                } else {
                    null
                }
            }
    }

    suspend fun evaluateAnswers(
        config: InterviewConfig,
        questionsAndAnswers: List<QuestionAnswer>
    ): Result<InterviewEvaluation> {
        return try {
            val prompt = createEvaluationPrompt(config, questionsAndAnswers)
            val request = GptRequest(
                messages = listOf(
                    GptMessage("user", prompt)
                ),
                max_tokens = 2000,
                temperature = 0.3 // Lower temperature for more consistent evaluation
            )

            val response = apiService.generateQuestions(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.let { gptResponse ->
                    val evaluationText = gptResponse.choices.firstOrNull()?.message?.content
                    val evaluation = parseEvaluation(evaluationText)
                    Result.success(evaluation)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createEvaluationPrompt(
        config: InterviewConfig,
        questionsAndAnswers: List<QuestionAnswer>
    ): String {
        val languageText = when (config.language) {
            Language.ENGLISH -> "English"
            Language.TURKISH -> "Turkish"
        }

        val difficultyText = when (config.difficulty) {
            DifficultyLevel.BASIC -> if (config.language == Language.TURKISH) "temel seviye" else "basic level"
            DifficultyLevel.INTERMEDIATE -> if (config.language == Language.TURKISH) "orta seviye" else "intermediate level"
            DifficultyLevel.ADVANCED -> if (config.language == Language.TURKISH) "ileri seviye" else "advanced level"
        }

        val qaText = questionsAndAnswers.mapIndexed { index, qa ->
            "Question ${index + 1}: ${qa.question}\nAnswer: ${qa.answer}\n"
        }.joinToString("\n")

        return when (config.language) {
            Language.ENGLISH -> """
            Evaluate the following interview answers for a ${config.profession} position at $difficultyText.
            
            Interview Details:
            - Position: ${config.profession}
            - Difficulty: $difficultyText
            - Language: $languageText
            
            Questions and Answers:
            $qaText
            
            Please provide a comprehensive evaluation in the following format:
            
            SCORE: [0-100]
            
            STRENGTHS:
            - [Strength 1]
            - [Strength 2]
            - [Strength 3]
            
            WEAKNESSES:
            - [Weakness 1]
            - [Weakness 2]
            - [Weakness 3]
            
            RECOMMENDATIONS:
            - [Recommendation 1]
            - [Recommendation 2]
            - [Recommendation 3]
            
            DETAILED_FEEDBACK:
            [Provide detailed feedback about the candidate's performance, highlighting specific answers and areas for improvement. This should be 2-3 paragraphs.]
            
            Evaluate based on:
            - Technical knowledge and accuracy
            - Communication skills
            - Problem-solving approach
            - Relevance of answers
            - Depth of understanding
            - Professional presentation
        """.trimIndent()

            Language.TURKISH -> """
            ${config.profession} pozisyonu için $difficultyText mülakat cevaplarını değerlendir.
            
            Mülakat Detayları:
            - Pozisyon: ${config.profession}
            - Zorluk: $difficultyText
            - Dil: Türkçe
            
            Sorular ve Cevaplar:
            $qaText
            
            Lütfen değerlendirmeyi aşağıdaki formatta sağlayın:
            
            PUAN: [0-100]
            
            GÜÇLÜ_YÖNLER:
            - [Güçlü Yön 1]
            - [Güçlü Yön 2]
            - [Güçlü Yön 3]
            
            ZAYIF_YÖNLER:
            - [Zayıf Yön 1]
            - [Zayıf Yön 2]
            - [Zayıf Yön 3]
            
            ÖNERİLER:
            - [Öneri 1]
            - [Öneri 2]
            - [Öneri 3]
            
            DETAYLI_GERİBİLDİRİM:
            [Adayın performansı hakkında detaylı geri bildirim, özellikle cevapları ve geliştirilmesi gereken alanları vurgulayarak. Bu 2-3 paragraf olmalı.]
            
            Şu kriterlere göre değerlendir:
            - Teknik bilgi ve doğruluk
            - İletişim becerileri
            - Problem çözme yaklaşımı
            - Cevapların konuyla ilgisi
            - Konuya hakimiyet
            - Profesyonel sunum
        """.trimIndent()
        }
    }

    private fun parseEvaluation(evaluationText: String?): InterviewEvaluation {
        if (evaluationText.isNullOrBlank()) {
            return InterviewEvaluation(0, emptyList(), emptyList(), emptyList(), "Değerlendirme başarısız")
        }

        val text = evaluationText.trim()

        val scoreRegex = Regex("(SCORE|PUAN):\\s*(\\d+)")
        val score = scoreRegex.find(text)?.groupValues?.get(2)?.toIntOrNull() ?: 0

        val strengthsRegex = Regex("(STRENGTHS|GÜÇLÜ_YÖNLER):\\s*\\n([\\s\\S]*?)\\n\\n")
        val strengthsText = strengthsRegex.find(text)?.groupValues?.get(2) ?: ""
        val strengths = strengthsText.split("\n")
            .filter { it.trim().startsWith("-") }
            .map { it.trim().removePrefix("-").trim() }

        val weaknessesRegex = Regex("(WEAKNESSES|ZAYIF_YÖNLER):\\s*\\n([\\s\\S]*?)\\n\\n")
        val weaknessesText = weaknessesRegex.find(text)?.groupValues?.get(2) ?: ""
        val weaknesses = weaknessesText.split("\n")
            .filter { it.trim().startsWith("-") }
            .map { it.trim().removePrefix("-").trim() }

        val recommendationsRegex = Regex("(RECOMMENDATIONS|ÖNERİLER):\\s*\\n([\\s\\S]*?)\\n\\n")
        val recommendationsText = recommendationsRegex.find(text)?.groupValues?.get(2) ?: ""
        val recommendations = recommendationsText.split("\n")
            .filter { it.trim().startsWith("-") }
            .map { it.trim().removePrefix("-").trim() }

        val detailedFeedbackRegex = Regex("(DETAILED_FEEDBACK|DETAYLI_GERİBİLDİRİM):\\s*\\n([\\s\\S]*)")
        val detailedFeedback = detailedFeedbackRegex.find(text)?.groupValues?.get(2)?.trim() ?: ""

        return InterviewEvaluation(
            overallScore = score,
            strengths = strengths,
            weaknesses = weaknesses,
            recommendations = recommendations,
            detailedFeedback = detailedFeedback
        )
    }

}