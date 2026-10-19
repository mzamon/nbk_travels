package com.example.nbk_travels.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    fun currentUser(): FirebaseUser? = auth.currentUser

    fun signIn(email: String, password: String): Task<AuthResult> =
        auth.signInWithEmailAndPassword(email.trim(), password)

    fun register(email: String, password: String): Task<AuthResult> =
        auth.createUserWithEmailAndPassword(email.trim(), password)

    fun signOut() = auth.signOut()
}
