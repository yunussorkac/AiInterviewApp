package com.yeslab.interviewapp.presentation.interview

import android.content.Context
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yeslab.interviewapp.domain.repository.InterviewRepository
import com.yeslab.interviewapp.model.InterviewConfig
import com.yeslab.interviewapp.model.InterviewEvaluation
import com.yeslab.interviewapp.model.QuestionAnswer
import com.yeslab.interviewapp.util.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InterviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val isEvaluating: Boolean = false,
    val evaluation: InterviewEvaluation? = null,
    val currentAnswer: String = "",
    val answers: List<String> = emptyList(),
    val isInterviewCompleted: Boolean = false
)
class InterviewScreenViewModel(
    private val repository: InterviewRepository,
    context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    private val _questions = MutableStateFlow<List<String>>(emptyList())
    val questions: StateFlow<List<String>> = _questions.asStateFlow()

    private var interviewConfig: InterviewConfig? = null

    private val speechManager = SpeechManager(context)

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingError = MutableStateFlow<String?>(null)
    val recordingError = _recordingError.asStateFlow()




    fun startVoiceInput() {
        _isRecording.value = true
        _recordingError.value = null

        speechManager.startListening(
            onResult = { result ->
                updateCurrentAnswer(result)
            },
            onError = { error ->
                _isRecording.value = false
                _recordingError.value = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No sound detected. Please try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No sound detected. Time expired."
                    SpeechRecognizer.ERROR_AUDIO -> "Error occurred while recording sound."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                    else -> "An error occurred. Please try again."
                }
            },
            onReadyForSpeech = {
                _recordingError.value = null
            },
            onEndOfSpeech = {
                _isRecording.value = false
            }
        )
    }

    fun stopVoiceInput() {
        speechManager.stopListening()
        _isRecording.value = false
    }

    fun generateQuestions(config: InterviewConfig) {
        interviewConfig = config
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.generateQuestions(config)
                .onSuccess { questions ->
                    _questions.value = questions
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentQuestionIndex = 0,
                        totalQuestions = questions.size,
                        answers = List(questions.size) { "" }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun updateCurrentAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(currentAnswer = answer)
    }

    fun saveCurrentAnswer() {
        val currentState = _uiState.value
        val updatedAnswers = currentState.answers.toMutableList()
        updatedAnswers[currentState.currentQuestionIndex] = currentState.currentAnswer

        _uiState.value = currentState.copy(
            answers = updatedAnswers,
            currentAnswer = ""
        )
    }

    fun nextQuestion() {
        saveCurrentAnswer()
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.totalQuestions - 1) {
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                currentAnswer = currentState.answers[nextIndex]
            )
        } else {
            completeInterview()
        }
    }

    fun previousQuestion() {
        saveCurrentAnswer()
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            val prevIndex = currentState.currentQuestionIndex - 1
            _uiState.value = currentState.copy(
                currentQuestionIndex = prevIndex,
                currentAnswer = currentState.answers[prevIndex]
            )
        }
    }

    private fun completeInterview() {
        val currentState = _uiState.value
        val questions = _questions.value

        if (questions.isEmpty() || currentState.answers.any { it.isBlank() }) {
            _uiState.value = currentState.copy(
                error = "Please answer all questions before completing the interview"
            )
            return
        }

        _uiState.value = currentState.copy(
            isInterviewCompleted = true,
            isEvaluating = true,
            error = null
        )

        viewModelScope.launch {
            interviewConfig?.let { config ->
                val questionsAndAnswers = questions.mapIndexed { index, question ->
                    QuestionAnswer(question, currentState.answers[index])
                }

                repository.evaluateAnswers(config, questionsAndAnswers)
                    .onSuccess { evaluation ->
                        repository.saveInterviewRecord(
                            config = config,
                            questionsAndAnswers = questionsAndAnswers,
                            evaluation = evaluation
                        )

                        _uiState.value = _uiState.value.copy(
                            isEvaluating = false,
                            evaluation = evaluation
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isEvaluating = false,
                            error = error.message ?: "Evaluation failed"
                        )
                    }
            }
        }
    }

    fun retryGeneration(config: InterviewConfig) {
        generateQuestions(config)
    }


    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }


}