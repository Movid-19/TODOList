package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.ui.theme.TODOListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    if (user == null) {
        LoginScreen(authViewModel)
    } else {
        val todoViewModel: TodoViewModel = viewModel(
            key = user!!.uid
        )
        Todolistpage(
            viewModel = todoViewModel,
            onSignOut = { authViewModel.signOut() }
        )
    }
}