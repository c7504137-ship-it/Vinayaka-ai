package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.VinayakaRepository
import com.example.ui.screens.MainCompanionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VinayakaViewModel
import com.example.ui.viewmodel.VinayakaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Build SQLite localized database instance
        val database = AppDatabase.getDatabase(applicationContext)

        // Build Repository binds
        val repository = VinayakaRepository(
            companionChatDao = database.companionChatDao(),
            mangaStoryboardDao = database.mangaStoryboardDao(),
            studyPlanDao = database.studyPlanDao(),
            agentLogDao = database.agentLogDao()
        )

        // ViewModel Factory construction
        val viewModelFactory = VinayakaViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[VinayakaViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainCompanionScreen(viewModel = viewModel)
            }
        }
    }
}
