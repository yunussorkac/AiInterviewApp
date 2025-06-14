package com.yeslab.interviewapp.model

data class QuestionAnswer(
    val question: String,
    val answer: String
)

data class InterviewEvaluation(
    val overallScore: Int,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>,
    val detailedFeedback: String
)