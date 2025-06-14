package com.yeslab.interviewapp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yeslab.interviewapp.data.local.InterviewQuestionEntity
import com.yeslab.interviewapp.data.local.InterviewRecordEntity
import com.yeslab.interviewapp.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailScreenViewModel(
    private val repository: HistoryRepository
) : ViewModel() {

    private val _interviewState = MutableStateFlow<InterviewDetailUiState>(InterviewDetailUiState.Loading)
    val interviewState = _interviewState.asStateFlow()

    fun loadInterview(id: Long) {
        viewModelScope.launch {
            try {
                val interviewData = repository.getInterview(id)
                _interviewState.value = InterviewDetailUiState.Success(
                    interview = interviewData.interview,
                    questions = interviewData.questions
                )
            } catch (e: Exception) {
                _interviewState.value = InterviewDetailUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

sealed class InterviewDetailUiState {
    data object Loading : InterviewDetailUiState()
    data class Success(
        val interview: InterviewRecordEntity,
        val questions: List<InterviewQuestionEntity>
    ) : InterviewDetailUiState()
    data class Error(val message: String) : InterviewDetailUiState()
}