package com.yeslab.interviewapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.yeslab.interviewapp.presentation.detail.DetailScreen
import com.yeslab.interviewapp.presentation.history.HistoryScreen
import com.yeslab.interviewapp.presentation.home.HomeScreen
import com.yeslab.interviewapp.presentation.home.SharedViewModel
import com.yeslab.interviewapp.presentation.interview.InterviewScreen
import com.yeslab.interviewapp.ui.Screens
import com.yeslab.interviewapp.ui.theme.InterviewAppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterviewAppTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold() { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MainNavigation(navController = navController)
        }
    }
}



@Composable
fun MainNavigation(navController: NavHostController) {

    val sharedViewModel = koinViewModel<SharedViewModel>()

    NavHost(navController, startDestination = Screens.Home) {

        composable<Screens.Home> {
            HomeScreen(navController, sharedViewModel)
        }

        composable<Screens.Interview> {
            InterviewScreen(navController, sharedViewModel)
        }

        composable<Screens.History>{
            HistoryScreen(navController)
        }

        composable<Screens.Detail> {
            val args = it.toRoute<Screens.Detail>()
            DetailScreen(args.id)

        }

    }
}