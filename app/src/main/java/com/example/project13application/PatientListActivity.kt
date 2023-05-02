package com.example.project13application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project13application.ui.models.Patient
import com.example.project13application.ui.models.Subscriber
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 *
 * This class is used to list all the basic patient
 * information in the RecycleView, including name,
 * ID and a button to access the PatientDetail Page.
 * The button intent determines whether the currently
 * logged in user is registered to the currently clicked user.
 *
 * Author: Rui Zeng, Wenbo Peng
 * BannerID: B00800727, B00916504
 * version: 3.0
 */
class PatientListActivity : AppCompatActivity(), PatientAdapter.OnViewDetailClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var patients: ArrayList<Patient>
    private lateinit var keys:ArrayList<String>
    private lateinit var database: DatabaseReference
    private lateinit var bottomNavigationView: BottomNavigationView
    private var selectedPatientId: String? = null

    private lateinit var auth: FirebaseAuth

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)
        //store data array
        patients = arrayListOf()
        keys = arrayListOf()

        auth = FirebaseAuth.getInstance()

        //recyclerview initial
        recyclerView = findViewById(R.id.p_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PatientAdapter(this, patients, keys)
        recyclerView.adapter = adapter

        //firebase initial
        database = FirebaseDatabase.getInstance().getReference("patients")
        database.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(dataSnapShot in snapshot.children){
                        //read each patient with their key in database
                        val patient = dataSnapShot.getValue(Patient::class.java)

                        val key = dataSnapShot.key.toString()
                        if(!patients.contains(patient)){
                            patients.add(patient!!)
                            keys.add(key!!)
                        }
                    }
                    //use the values to build the view
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PatientListActivity, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        // navbar
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    val intent = Intent(this, PatientListActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_details -> {
                    if (selectedPatientId == null) {
                        Toast.makeText(this@PatientListActivity, "Please select a patient first", Toast.LENGTH_SHORT).show()
                        return@setOnItemSelectedListener true
                    }
                    val intent = Intent(this, DetailPatientActivity::class.java)
                    intent.putExtra("patientId", selectedPatientId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    // Implement the click listener function
    override fun onViewDetailClick(patientId: String) {
        selectedPatientId = patientId
        checkSubscriberAndNavigate(patientId)
    }

    fun checkSubscriberAndNavigate(patientId: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("patients")
        database.child(patientId).child("subscribers").child(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // If the subscriber exists, navigate to DetailPatientActivity
                        val intent = Intent(this@PatientListActivity, DetailPatientActivity::class.java)
                        intent.putExtra("patientId", patientId)
                        startActivity(intent)
                    } else {
                        // Ask the user if they want to subscribe to the patient
                        AlertDialog.Builder(this@PatientListActivity)
                            .setTitle("Subscribe to Patient")
                            .setMessage("You are not subscribed to this patient. Would you like to subscribe?")
                            .setPositiveButton("Yes") { _, _ ->
                                // Subscribe to the patient and navigate to DetailPatientActivity
                                createSubscriber { subscriber ->
                                    if (subscriber != null) {
                                        database.child(patientId).child("subscribers").child(currentUserUid).setValue(subscriber)
                                            .addOnSuccessListener {
                                                val intent = Intent(this@PatientListActivity, DetailPatientActivity::class.java)
                                                intent.putExtra("patientId", patientId)
                                                startActivity(intent)
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(this@PatientListActivity, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(this@PatientListActivity, "Error fetching subscriber details", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PatientListActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
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