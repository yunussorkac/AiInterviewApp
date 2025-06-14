package com.yeslab.interviewapp.presentation.home

import androidx.lifecycle.ViewModel
import com.yeslab.interviewapp.model.InterviewConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel () : ViewModel() {

    private val _selectedInterview = MutableStateFlow<InterviewConfig?>(null)
    val selectedInterview: StateFlow<InterviewConfig?> = _selectedInterview.asStateFlow()

    fun selectInterview(interview: InterviewConfig) {
        _selectedInterview.value = interview
    }

}