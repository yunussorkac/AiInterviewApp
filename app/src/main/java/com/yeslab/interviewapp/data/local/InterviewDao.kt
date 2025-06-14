package com.yeslab.interviewapp.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {
    @Insert
    suspend fun insertInterviewRecord(record: InterviewRecordEntity): Long

    @Insert
    suspend fun insertInterviewQuestions(questions: List<InterviewQuestionEntity>)

    @Transaction
    @Query("SELECT * FROM interview_records ORDER BY date DESC")
    fun getAllInterviewsWithQuestions(): Flow<List<InterviewWithQuestionsRelation>>

    @Transaction
    @Query("SELECT * FROM interview_records WHERE id = :interviewId")
    suspend fun getInterviewWithQuestions(interviewId: Long): InterviewWithQuestionsRelation
}

data class InterviewWithQuestionsRelation(
    @Embedded val interview: InterviewRecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "interviewId"
    )
    val questions: List<InterviewQuestionEntity>
)

