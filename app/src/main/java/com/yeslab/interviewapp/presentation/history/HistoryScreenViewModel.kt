package com.yeslab.interviewapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yeslab.interviewapp.domain.repository.HistoryRepository
import com.yeslab.interviewapp.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HistoryScreenViewModel(
    private val repository: HistoryRepository
) : ViewModel() {

    val interviews = repository.getAllInterviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}