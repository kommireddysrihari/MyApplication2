package com.example.myapplication.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String
)

sealed class AuthResult {
    data class Success(val user: UserSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthRepository(private val context: Context) {

    private var firebaseAuth: FirebaseAuth? = null
    private var isFirebaseAvailable = false

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            isFirebaseAvailable = true
            val fbUser = firebaseAuth?.currentUser
            if (fbUser != null) {
                _currentUser.value = UserSession(
                    uid = fbUser.uid,
                    email = fbUser.email ?: "",
                    displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User"
                )
            }
        } catch (e: Exception) {
            // Firebase not initialized yet (e.g. google-services.json missing). Fall back gracefully.
            isFirebaseAvailable = false
        }
    }

    suspend fun login(email: String, pass: String, onResult: (AuthResult) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onResult(AuthResult.Error("Please enter email and password"))
            return
        }

        if (isFirebaseAvailable && firebaseAuth != null) {
            onResult(AuthResult.Loading)
            firebaseAuth!!.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        val session = UserSession(
                            uid = user.uid,
                            email = user.email ?: email,
                            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User"
                        )
                        _currentUser.value = session
                        onResult(AuthResult.Success(session))
                    } else {
                        onResult(AuthResult.Error("Failed to fetch user profile."))
                    }
                }
                .addOnFailureListener { e ->
                    onResult(AuthResult.Error(e.localizedMessage ?: "Authentication failed"))
                }
        } else {
            // Local Session Fallback for quick local testing without Firebase setup
            val session = UserSession(
                uid = "demo_user_123",
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
            _currentUser.value = session
            onResult(AuthResult.Success(session))
        }
    }

    suspend fun register(name: String, email: String, pass: String, onResult: (AuthResult) -> Unit) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            onResult(AuthResult.Error("Please fill in all fields"))
            return
        }
        if (pass.length < 6) {
            onResult(AuthResult.Error("Password must be at least 6 characters"))
            return
        }

        if (isFirebaseAvailable && firebaseAuth != null) {
            onResult(AuthResult.Loading)
            firebaseAuth!!.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profileUpdates)

                        val session = UserSession(
                            uid = user.uid,
                            email = user.email ?: email,
                            displayName = name
                        )
                        _currentUser.value = session
                        onResult(AuthResult.Success(session))
                    } else {
                        onResult(AuthResult.Error("Registration succeeded but profile failed."))
                    }
                }
                .addOnFailureListener { e ->
                    onResult(AuthResult.Error(e.localizedMessage ?: "Registration failed"))
                }
        } else {
            // Local Session Fallback
            val session = UserSession(
                uid = "demo_user_123",
                email = email,
                displayName = name
            )
            _currentUser.value = session
            onResult(AuthResult.Success(session))
        }
    }

    fun logout() {
        if (isFirebaseAvailable && firebaseAuth != null) {
            firebaseAuth?.signOut()
        }
        _currentUser.value = null
    }

    fun isFirebaseConfigured(): Boolean = isFirebaseAvailable
}
