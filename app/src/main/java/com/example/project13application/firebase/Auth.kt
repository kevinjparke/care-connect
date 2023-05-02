//package com.example.project13application.firebase
//
//import android.content.ContentValues.TAG
//import android.util.Log
//import com.google.firebase.auth.AuthResult
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.tasks.await
//
//import com.google.firebase.ktx.Firebase
//
//class Auth {
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    /**
//     * Signs up with callbacks so callers can handle success or failure accordingly
//     */
//    suspend fun signUp(email: String, password: String): String {
//        return try {
//            val result = auth.createUserWithEmailAndPassword(email, password).await()
//            result.user?.uid ?: throw Exception("User is null")
//        } catch (e: Exception) {
//            throw Exception("Sign-up failed: ${e.message}")
//        }
//    }
//
//    /**
//     * Signs in user with callbacks so callers can handle success or failure accordingly
//     */
//
//    fun signOut() {
//        auth.signOut()
//    }
//}