package com.example.project13application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.project13application.databinding.ActivitySubscribeActivityBinding
import com.example.project13application.ui.models.Patient
import com.example.project13application.ui.models.Subscriber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


/**
 *
 * This Class is used to subscribe patients.
 * All Family members that do not subscribe
 * to any patient will be redirected to this page.
 * The user needs to enter an 8-digit confirmation
 * code to subscribe corresponding patients.
 *
 * Author: Kevin Parke, Wenbo Peng
 * BannerID: B00905552, B00916504
 * version: 2.0
 */
class SubscribeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySubscribeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscribeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.subscribeButton.setOnClickListener {
            val enteredCode = binding.subscriptionCodeEditText.text.toString()
            searchSubscriptionCode(enteredCode)
        }
    }

    private fun searchSubscriptionCode(subscriptionCode: String) {
        val database = FirebaseDatabase.getInstance()
        val patientsRef = database.getReference("patients")
        val query = patientsRef.orderByChild("subscriptionCode").equalTo(subscriptionCode)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val patientEntry = snapshot.children.first()
                    val patient = patientEntry.getValue(Patient::class.java)
                    val patientId = patientEntry.key

                    if (patient != null && patientId != null) {
                        subscriberPatient(patientId)
                    }

                } else {
                    showSubscriptionError()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showSubscriptionError()
            }
        })
    }

    private fun subscriberPatient(patientId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            val database = FirebaseDatabase.getInstance()
            val subscriberRef = database.getReference("subscribers")
            val patientSubscribersRef = database.getReference("patients/$patientId/subscribers")

            val subscriptionCode = patientId.takeLast(8).uppercase()
            subscriberRef.child(currentUserId).child("subscriptionCode").setValue(subscriptionCode)

            subscriberRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val subscriber = snapshot.getValue(Subscriber::class.java)

                    if (subscriber != null) {
                        patientSubscribersRef.child(currentUserId).setValue(subscriber)
                            .addOnSuccessListener {
                                val intent = Intent(this@SubscribeActivity, DetailPatientActivity::class.java)
                                intent.putExtra("patientId", patientId)
                                startActivity(intent)
                            }
                            .addOnFailureListener { exception ->
                                showToast("Error")
                            }
                    } else {
                        showToast("Error")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error")
                }
            })
        } else {
            showToast("Error")
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val subscriberRef = Firebase.database.reference.child("subscribers").child(currentUser.uid)
            subscriberRef.get().addOnSuccessListener { snapshot ->
                val subscriber = snapshot.getValue(Subscriber::class.java)
                if (subscriber?.subscriptionCode?.isEmpty() == false) {
                    val intent = Intent(this@SubscribeActivity, DetailPatientActivity::class.java)
                    intent.putExtra("patientId", subscriber.subscriptionCode)
                    startActivity(intent)
                }
            }.addOnFailureListener { error ->
                Log.e("firebase", "Error getting data", error)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSubscriptionError() {
        Toast.makeText(this, "Invalid subscription code! Please try again", Toast.LENGTH_SHORT).show()
        binding.subscriptionCodeEditText.error = "Invalid subscription code!"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}







