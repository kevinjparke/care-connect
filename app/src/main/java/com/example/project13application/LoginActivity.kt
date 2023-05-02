package com.example.project13application //<- New Change

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project13application.ui.models.Patient
import com.example.project13application.ui.models.Subscriber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 *
 * This is the login page based on Firebase authentication
 * and can determine the login role (Family member or Caregiver)
 * and can determine if the login role has any subscriptions.
 *
 * Author: Wenbo Peng
 * BannerID: B00916504
 * version: 2.0
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupButton = findViewById<TextView>(R.id.navigateToSignUpButton) //<- New change

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }

        //Navigate to sign up if user does not have an account
        signupButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Login", "Login was successful, proceed to fetch subscriber details")
                    fetchSubscriberDetails()
                } else {
                    Log.e("Login", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Login", "Login failed with an exception: ${e.message}", e)
                Toast.makeText(this, "Login failed with an exception.", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchSubscriberDetails() {
        val currentUser = auth.currentUser
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        if (currentUser != null) {
            Log.d("Login", "Fetching subscriber details for user: ${currentUser.uid}")
            val subscribersRef = FirebaseDatabase.getInstance().getReference("subscribers")
            subscribersRef.child(currentUserUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val subscriber = snapshot.getValue(Subscriber::class.java)
                            //val subscriber = data.getValue(Subscriber::class.java)
                        if (subscriber != null) {
                                // Check if the current subscriber is a family member and has no subscriptionCode
                            if (!(subscriber.canEdit) && subscriber.subscriptionCode.isNullOrEmpty()) {
                                Log.d("Login", "Navigating to SubscribeActivity")
                                startActivity(Intent(this@LoginActivity, SubscribeActivity::class.java))
                            } else if (!(subscriber.canEdit) && subscriber.subscriptionCode.length == 8){
                                // Fetch the patient based on the subscriptionCode
                                val patientsRef = FirebaseDatabase.getInstance().getReference("patients")

                                patientsRef.orderByChild("subscriptionCode").equalTo(subscriber.subscriptionCode).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(patientSnapshot: DataSnapshot) {
                                        if (patientSnapshot.exists()) {
                                            for (patientData in patientSnapshot.children) {
                                                val patient = patientData.getValue(Patient::class.java)
                                                if (patient != null) {
                                                    Log.d("Login", "Navigating to DetailPatientActivity")
                                                    val intent = Intent(this@LoginActivity, DetailPatientActivity::class.java)
                                                    intent.putExtra("patientId", patientData.key) // Pass the patient ID to DetailPatientActivity
                                                    startActivity(intent)
                                                    finish()
                                                    break
                                                }
                                            }
                                        } else {
                                            Log.e("Login", "Patient not found for subscriptionCode: ${subscriber.subscriptionCode}")
                                        }
                                    }

                                    override fun onCancelled(patientError: DatabaseError) {
                                        Log.e("Login", "Failed to fetch patient details: ${patientError.message}")
                                    }
                                })


                            } else if (subscriber.canEdit && subscriber.subscriptionCode.isNullOrEmpty()){
                                startActivity(Intent(this@LoginActivity, AddPatientActivity::class.java))

                            } else {
                                Log.d("Login", "Navigating to PatientListActivity")
                                startActivity(Intent(this@LoginActivity, PatientListActivity::class.java))
                            }
                            finish()

                        }
                    } else {
                        Log.e("Login", "Subscriber snapshot does not exist")
                        Toast.makeText(this@LoginActivity, "Subscriber not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Login", "Failed to fetch subscriber details: ${error.message}")
                }
            })
        } else {
            Log.e("Login", "Current user is null")
        }
    }



}
