package com.yeslab.interviewapp.presentation.interview

import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.yeslab.interviewapp.model.Language
import com.yeslab.interviewapp.presentation.home.SharedViewModel
import com.yeslab.interviewapp.presentation.interview.components.EvaluationResult
import com.yeslab.interviewapp.util.PermissionUtils
import org.koin.androidx.compose.koinViewModel

@Composable
fun InterviewScreen(navController: NavHostController, sharedViewModel: SharedViewModel) {

    val context = LocalContext.current

    val interviewViewModel = koinViewModel<InterviewScreenViewModel>()
    val interviewConfig by sharedViewModel.selectedInterview.collectAsStateWithLifecycle()
    val uiState by interviewViewModel.uiState.collectAsStateWithLifecycle()
    val questions by interviewViewModel.questions.collectAsStateWithLifecycle()

    val isRecording by interviewViewModel.isRecording.collectAsStateWithLifecycle()
    val recordingError by interviewViewModel.recordingError.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            interviewViewModel.startVoiceInput()
        } else {
            Toast.makeText(
                context,
                "Permission needed for voice input",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(interviewConfig) {
        interviewConfig?.let { config ->
            interviewViewModel.generateQuestions(config)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Interview Questions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        if (uiState.totalQuestions > 0) {
            LinearProgressIndicator(
                progress = {
                    (uiState.currentQuestionIndex + 1f) / uiState.totalQuestions
                },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.totalQuestions}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Generating questions...")
                    }
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error generating questions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.error.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            interviewConfig?.let { config ->
                                interviewViewModel.retryGeneration(config)
                            }
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }

            questions.isNotEmpty() -> {
                if (uiState.isEvaluating) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Evaluating your answers...")
                        }
                    }
                } else if (uiState.evaluation != null) {
                    EvaluationResult(
                        evaluation = uiState.evaluation!!,
                        onRetry = {
                            navController.navigateUp()
                        },
                        language = interviewConfig?.language ?: Language.TURKISH
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxSize()
                        ) {
                            Text(
                                text = questions[uiState.currentQuestionIndex],
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            IconButton(
                                onClick = {
                                    if (isRecording) {
                                        interviewViewModel.stopVoiceInput()
                                    } else {
                                        when {
                                            PermissionUtils.checkMicrophonePermission(context) -> {
                                                interviewViewModel.startVoiceInput()
                                            }
                                            else -> {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicNone,
                                    contentDescription = "Voice Input",
                                    tint = if (isRecording) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }

                            TextField(
                                value = uiState.currentAnswer,
                                onValueChange = { interviewViewModel.updateCurrentAnswer(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                label = { Text("Your Answer") },
                                placeholder = { Text("Type your answer here or use voice input...") },
                                isError = recordingError != null
                            )

                            recordingError?.let { error ->
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if (isRecording) {
                                Text(
                                    text = "Konuşun...",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { interviewViewModel.previousQuestion() },
                            enabled = uiState.currentQuestionIndex > 0
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Previous")
                        }

                        if (uiState.currentQuestionIndex == uiState.totalQuestions - 1) {
                            Button(
                                onClick = { interviewViewModel.nextQuestion() }
                            ) {
                                Text("Finish")
                            }
                        } else {
                            Button(
                                onClick = { interviewViewModel.nextQuestion() }
                            ) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}



