package com.yeslab.interviewapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "interview_records")
data class InterviewRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val profession: String,
    val difficulty: String,
    val language: String,
    val overallScore: Int,
    val strengths: String,
    val weaknesses: String,
    val recommendations: String,
    val detailedFeedback: String
)

@Entity(tableName = "interview_questions")
data class InterviewQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val interviewId: Long,
    val questionNumber: Int,
    val question: String,
    val answer: String
)