package com.yeslab.interviewapp.di

import androidx.room.Room
import com.yeslab.interviewapp.data.local.InterviewDatabase
import com.yeslab.interviewapp.data.remote.GptApiService
import com.yeslab.interviewapp.domain.repository.HistoryRepository
import com.yeslab.interviewapp.domain.repository.InterviewRepository
import com.yeslab.interviewapp.presentation.detail.DetailScreenViewModel
import com.yeslab.interviewapp.presentation.history.HistoryScreenViewModel
import com.yeslab.interviewapp.presentation.home.SharedViewModel
import com.yeslab.interviewapp.presentation.interview.InterviewScreenViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Modules {

    val roomModule = module {
        single {
            Room.databaseBuilder(
                get(),
                InterviewDatabase::class.java,
                "interview_database"
            ).build()
        }

        single { get<InterviewDatabase>().interviewDao() }
    }

    val viewModelModule = module {
        viewModel { SharedViewModel() }
        viewModel { InterviewScreenViewModel(get(),get()) }
        viewModel { HistoryScreenViewModel(get()) }
        viewModel { DetailScreenViewModel(get()) }

    }

    val repositoryModule = module {
        single { InterviewRepository(get(),get()) }
        single { HistoryRepository(get()) }
    }


    val networkModule = module {
        single<Retrofit> {
            Retrofit.Builder()
                .baseUrl("https://api.openai.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                )
                .build()
        }

        single<GptApiService> {
            get<Retrofit>().create(GptApiService::class.java)
        }
    }


}