package com.yeslab.interviewapp.model

data class InterviewConfig(
    val profession: String,
    val difficulty: DifficultyLevel,
    val language: Language,
    val questionCount: Int
)


enum class DifficultyLevel(val label: String) {
    BASIC("Basic"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

enum class Language(val label: String) {
    ENGLISH("English"),
    TURKISH("Turkish")
}

