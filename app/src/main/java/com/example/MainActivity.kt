package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.DoodleOverlay
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MainViewModel = viewModel()
      val authState by viewModel.authState.collectAsState()
      val isDark by viewModel.isDarkMode.collectAsState()
      val isDoodleModeEnabled by viewModel.isDoodleModeEnabled.collectAsState()

      MyApplicationTheme(darkTheme = isDark) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (authState) {
                    is AuthState.Authenticated -> {
                        MainScreen(viewModel = viewModel)
                    }
                    else -> {
                        AuthScreen(viewModel = viewModel)
                    }
                }
            }

            if (isDoodleModeEnabled) {
                DoodleOverlay()
            }
        }
      }
    }
  }
}

