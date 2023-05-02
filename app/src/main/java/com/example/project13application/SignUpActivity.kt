package com.example.project13application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.project13application.databinding.ActivitySignUpBinding
import com.example.project13application.ui.models.Subscriber
import com.example.project13application.ui.models.SubscriberType
import com.example.project13application.utilities.FormValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 *
 * This class is used to create a Firebase
 * authentication-based registration function,
 * all registered users will be stored in Firebase
 * authentication users and as a Subscriber into Firebase.
 *
 * Author: Kevin Parke
 * BannerID: B00905552
 * version: 2.0
 */
class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var selectedSubscriberType: SubscriberType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val subscriberTypeSpinner: Spinner = findViewById(R.id.subscriberTypeSpinner)
        val subscriberTypes = resources.getStringArray(R.array.subscriber_types)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subscriberTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subscriberTypeSpinner.adapter = spinnerAdapter

        val signupButton: Button = binding.signupButton
        val signinText: TextView = binding.navigateToSignInButton

        subscriberTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSubscriberType = if (position == 0) SubscriberType.FAMILY_MEMBER else SubscriberType.CAREGIVER
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSubscriberType = SubscriberType.FAMILY_MEMBER
            }
        }

        signupButton.setOnClickListener {
            val emailText = binding.editTextEmailAddress.text.toString()
            val passwordText= binding.editTextPassword.text.toString()
            val confirmPasswordText = binding.editTextConfirmPassword.text.toString()

            if (!FormValidator.isValidEmailAddress(emailText)){
                binding.editTextEmailAddress.error = "Enter a valid email"
            }

            if (!FormValidator.isValidatePassword(passwordText)) {
                binding.editTextPassword.error = "Minimum password requirements: 1 uppercase letter, 1 number, 8 characters"
            }

            if (!FormValidator.isPasswordMatched(passwordText, confirmPasswordText)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            }

            if (FormValidator.isFormComplete(emailText, passwordText, confirmPasswordText)){
                auth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val canEdit = selectedSubscriberType == SubscriberType.CAREGIVER
                                val subscriber = Subscriber(emailText, selectedSubscriberType, canEdit)
                                val database = FirebaseDatabase.getInstance()
                                val subscribersRef = database.getReference("subscribers")

                                subscribersRef.child(userId).setValue(subscriber)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            if (canEdit){
                                                startActivity(Intent(this@SignUpActivity, AddPatientActivity::class.java))
                                                finish()
                                            } else {
                                                startActivity(Intent(this@SignUpActivity, SubscribeActivity::class.java))
                                                finish()
                                            }
                                        } else {
                                            Toast.makeText(this, "Failed to create subscriber.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Failed to create user.", Toast.LENGTH_SHORT).show()
                            }
                            Log.d("com.example.project13application.SignUpActivity", "createUserWithEmail:success")
                            //startActivity(Intent(this@SignUpActivity, TestActivity::class.java))
                            finish()
                        } else {
                            Log.w("com.example.project13application.SignUpActivity", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        signinText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
