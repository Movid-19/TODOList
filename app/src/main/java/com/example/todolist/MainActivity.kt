package com.example.todolist

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.auth.LoginScreen
import com.example.todolist.notification.NotificationHelper
import com.example.todolist.ui.screens.MainScreen
import com.example.todolist.ui.theme.TODOListTheme
import com.example.todolist.viewmodel.AuthViewModel
import com.example.todolist.viewmodel.TodoViewModel
import com.example.todolist.viewmodel.TodoViewModelFactory
import androidx.core.app.ActivityCompat
import com.example.todolist.auth.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        setContent {
            TODOListTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val application = LocalContext.current.applicationContext as android.app.Application

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(application)
    )

    val user by authViewModel.user.collectAsState()

    var showSignUp by remember { mutableStateOf(false) }

    if (user != null) {
        val currentUser = user
        val todoViewModel: TodoViewModel = viewModel(
            key = currentUser?.uid,
            factory = TodoViewModelFactory(application)
        )
        MainScreen(
            viewModel = todoViewModel,
            onSignOut = { authViewModel.signOut() }
        )
    } else {
        if (showSignUp) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { showSignUp = false }
            )
        } else {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { showSignUp = true }
            )
        }
    }
}