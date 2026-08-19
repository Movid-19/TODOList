package com.example.todolist.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("DEPRECATION")
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(getApplication(), gso)
    }

    fun signInWithGoogle(idToken: String) {
        isLoading.value = true
        errorMessage.value = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                } else {
                    Log.e("AuthVM", "Google sign-in failed", task.exception)
                    errorMessage.value = task.exception?.localizedMessage ?: "Google sign-in failed"
                    _user.value = null
                }
            }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage.value = "Email and password cannot be empty"
            return
        }
        isLoading.value = true
        errorMessage.value = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                } else {
                    Log.e("AuthVM", "Email login failed", task.exception)
                    errorMessage.value = task.exception?.localizedMessage ?: "Login failed"
                }
            }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage.value = "Email and password cannot be empty"
            return
        }
        if (password.length < 6) {
            errorMessage.value = "Password must be at least 6 characters"
            return
        }
        isLoading.value = true
        errorMessage.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                } else {
                    Log.e("AuthVM", "Sign-up failed", task.exception)
                    errorMessage.value = task.exception?.localizedMessage ?: "Sign-up failed"
                }
            }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            errorMessage.value = "Enter your email address"
            return
        }
        isLoading.value = true
        errorMessage.value = null
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    errorMessage.value = "Password reset email sent!"
                } else {
                    Log.e("AuthVM", "Reset password failed", task.exception)
                    errorMessage.value = task.exception?.localizedMessage ?: "Failed to send reset email"
                }
            }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        googleSignInClient.revokeAccess()
        _user.value = null
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(application) as T
        }
    }

    companion object {
        const val WEB_CLIENT_ID = "923694756694-c7rur84f3o2h5opf12t01jq82oe6cg9a.apps.googleusercontent.com"
    }
}