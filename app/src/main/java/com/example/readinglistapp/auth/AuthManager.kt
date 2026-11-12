package com.example.readinglistapp.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private var firebaseAuth: FirebaseAuth? = null

    companion object {
        const val WEB_CLIENT_ID = "904096581579-vhcfc09p7cuhk62occhim7mp57j647bt.apps.googleusercontent.com"

        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context).also { instance = it }
            }
        }
    }

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
            }
        } catch (e: Exception) {
            firebaseAuth = null
        }
    }

    val currentUserEmail: String?
        get() {
            val fbUser = firebaseAuth?.currentUser
            if (fbUser != null) return fbUser.email ?: fbUser.displayName ?: "Google User"
            return prefs.getString("local_user_email", null)
        }

    val isLoggedIn: Boolean
        get() = currentUserEmail != null

    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.localizedMessage ?: "Authentication failed") }
        } else {
            if (email.contains("@") && pass.length >= 6) {
                prefs.edit().putString("local_user_email", email).apply()
                onSuccess()
            } else {
                onFailure("Invalid email or password (min 6 chars)")
            }
        }
    }

    fun signUp(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.localizedMessage ?: "Sign up failed") }
        } else {
            if (email.contains("@") && pass.length >= 6) {
                prefs.edit().putString("local_user_email", email).apply()
                onSuccess()
            } else {
                onFailure("Invalid email or password (min 6 chars)")
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String, userEmailHint: String? = null, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.localizedMessage ?: "Google Sign-In failed") }
        } else {
            val email = userEmailHint ?: "google.user@example.com"
            prefs.edit().putString("local_user_email", email).apply()
            onSuccess()
        }
    }

    fun setLocalUser(email: String) {
        prefs.edit().putString("local_user_email", email).apply()
    }

    fun signOut() {
        firebaseAuth?.signOut()
        prefs.edit().remove("local_user_email").apply()
    }
}
