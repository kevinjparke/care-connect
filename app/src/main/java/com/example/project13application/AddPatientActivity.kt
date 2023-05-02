package com.example.project13application

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.project13application.databinding.ActivityAddPatientBinding
import com.example.project13application.ui.models.Diary
import com.example.project13application.ui.models.Patient
import com.example.project13application.ui.models.Subscriber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.util.*

/**
 *
 * This class is used to add a new Patient
 * and a new Daily Diary, only Caregiver can
 * access this page. caregiver will automatically
 * subscribe this patient.
 *
 * Author: Wenbo Peng
 * BannerID: B00916504
 * version: 3.0
 */
class AddPatientActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityAddPatientBinding

    private fun createSubscriber(completion: (Subscriber?) -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid == null) {
            completion(null)
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("subscribers")
        database.child(currentUserUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val subscriber = snapshot.getValue(Subscriber::class.java)
                    completion(subscriber)
                } else {
                    completion(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                completion(null)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val patientFirstName = findViewById<EditText>(R.id.patientFirstName)
        val patientLastName = findViewById<EditText>(R.id.patientLastName)
        val diaryContent = findViewById<EditText>(R.id.diaryContent)
        val addDataButton = findViewById<Button>(R.id.addDataButton)

        // Display today's date in layout
        val today = Clock.System.todayAt(TimeZone.currentSystemDefault())
        binding.diaryDate.text = getString(R.string.diary_date, today.toString())

        addDataButton.setOnClickListener {

            val firstName = patientFirstName.text.toString()
            val lastName = patientLastName.text.toString()
            val content = diaryContent.text.toString()

            // Create objects
            val patient = Patient(id = "", username = "", firstName = firstName, lastName = lastName)
            val diaryEntry = Diary.create(id = "", date = today, content = content)

            // Get database reference
            val database = FirebaseDatabase.getInstance()

            val patientRef = database.getReference("patients")

            // Add patient to Firebase, Firebase auto-generated IDs
            val patientId = patientRef.push().key ?: ""

            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

            // Generate 8 character key using code
            val generateSubscriptionCode = patientId.takeLast(8).uppercase()
            patient.subscriptionCode = generateSubscriptionCode

            patientRef.child(patientId).setValue(patient).addOnCompleteListener {
                if (it.isSuccessful) {
                    // Update the caregiver's Subscriber object in the database
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserId != null) {
                        val subscriberRef = FirebaseDatabase.getInstance().getReference("subscribers")
                        subscriberRef.child(currentUserId).child("subscriptionCode").setValue(patient.subscriptionCode)

                        // Add the caregiver's ID to the patient's subscribers subset
                        val patientSubscribersRef = FirebaseDatabase.getInstance().getReference("patients/$patientId/subscribers")


                        createSubscriber { subscriber ->
                            if (subscriber != null) {
                                patientRef.child(patientId).child("subscribers").child(currentUserUid).setValue(subscriber)
                                    .addOnSuccessListener {
                                        val intent = Intent(this@AddPatientActivity, DetailPatientActivity::class.java)
                                        intent.putExtra("patientId", patientId)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this@AddPatientActivity, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this@AddPatientActivity, "Error fetching subscriber details", Toast.LENGTH_SHORT).show()
                            }
                        }


                        patientSubscribersRef.child(currentUserId).setValue(true)
                    }

                    Toast.makeText(this, "Patient saved successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save the patient.", Toast.LENGTH_SHORT).show()
                }
            }

            // Diary and subscriber are under the patient
            val diaryRef = patientRef.child(patientId).child("diary entry")

            // Add diary and subscriber to Firebase, Firebase auto-generated IDs
            val diaryId = patientRef.child(patientId).child("diary entry").push().key ?: ""
            diaryRef.child(diaryId).setValue(diaryEntry)
                .addOnSuccessListener {
                    showToast("Patient added successfully")
                }
                .addOnFailureListener { exception ->
                    showToast("Failed to add patient: ${exception.localizedMessage}")
                }

        }

    }

    // ** if you want to skip login just comment onStart() code block
    // Check if a user is logged in. If not, navigate to the com.example.project13application.LoginActivity
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}

