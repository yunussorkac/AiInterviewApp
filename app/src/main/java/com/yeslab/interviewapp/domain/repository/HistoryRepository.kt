package com.yeslab.interviewapp.domain.repository

import com.yeslab.interviewapp.data.local.InterviewDao
import com.yeslab.interviewapp.data.local.InterviewWithQuestionsRelation
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val interviewDao: InterviewDao
) {

    fun getAllInterviews(): Flow<List<InterviewWithQuestionsRelation>> {
        return interviewDao.getAllInterviewsWithQuestions()
    }

    suspend fun getInterview(id: Long): InterviewWithQuestionsRelation {
        return interviewDao.getInterviewWithQuestions(id)
    }



}