package com.example.project13application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project13application.ui.models.Diary
import com.example.project13application.ui.models.Patient
import com.example.project13application.ui.models.Subscriber
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 *
 * This Class is used to display patient details,
 * including their full name, the Diary Caregiver
 * wrote for them and the registrant information
 * for this patient. Three RecycleViews are used
 * on the front end.
 *
 * Author: Rui Zeng, Meet Kumer Patel
 * BannerID: B00800727, B00857644
 * version: 3.0
 */

class DetailPatientActivity : AppCompatActivity() {
    private lateinit var note_recyclerView: RecyclerView
    private lateinit var sub_fam_recyclerView: RecyclerView
    private lateinit var sub_car_recyclerView: RecyclerView
    private lateinit var diarys: ArrayList<Diary>
    private lateinit var sub_mem:ArrayList<Subscriber>
    private lateinit var sub_car:ArrayList<Subscriber>
    private lateinit var database: DatabaseReference
    //private lateinit var child_key: String
    private lateinit var user: TextView

    private lateinit var bottomNavigationView: BottomNavigationView


    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_patient)

        auth = FirebaseAuth.getInstance()

        // navbar
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.menu.findItem(R.id.action_details).isChecked = true
        bottomNavigationView.selectedItemId = R.id.action_details

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    val intent = Intent(this, PatientListActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_details -> {
                    true // do nothing
                }
                else -> false
            }
        }

        // retrieve the patient's ID from the intent extras (not implemented yet)
        val patientId = intent.getStringExtra("patientId")

        //get patient key
        val child_key = intent.getStringExtra("patientId").toString()
        Log.d("DetailPatientActivity", "Received child_key: $child_key")
        user = findViewById(R.id.p_username)

        //initial arraylist
        diarys = arrayListOf()
        sub_mem = arrayListOf()
        sub_car = arrayListOf()

        // //recyclerview initial
        //diary
        note_recyclerView = findViewById(R.id.p_notes)
        note_recyclerView.layoutManager = LinearLayoutManager(this)
        val note_adapter = DiaryAdapter(diarys)
        note_recyclerView.adapter = note_adapter

        //family member
        sub_fam_recyclerView = findViewById(R.id.p_family_members)
        sub_fam_recyclerView.layoutManager = LinearLayoutManager(this)
        val fam_adapter = subAdapter(sub_mem)
        sub_fam_recyclerView.adapter = fam_adapter

        //caregivers
        sub_car_recyclerView = findViewById(R.id.p_caregivers)
        sub_car_recyclerView.layoutManager = LinearLayoutManager(this)
        val car_adapter = subAdapter(sub_car)
        sub_car_recyclerView.adapter = car_adapter

        //initialize db
        database = FirebaseDatabase.getInstance().getReference("patients").child(child_key)
        val database_patient = database;
        database_patient.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DetailPatientActivity", "DataSnapshot: $snapshot")
                val patient = snapshot.getValue(Patient::class.java)
                if(snapshot.exists()){

                    // set full name in patient detail page
                    val fullName = "${patient?.firstName} ${patient?.lastName}"
                    user.text = fullName

                    Log.d("PatientFullName", "Full Name: $fullName")
                }else {
                    Log.e("PatientFullName", "Patient not found")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PatientFullName", "Error: databaseError")
            }
        })


        //read all diaries
        val database_diary = database.child("diary entry")
        database_diary.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(dataSnapShot in snapshot.children){
                        //read each patient with their key in database
                        val diary = dataSnapShot.getValue(Diary::class.java)
                        if(!diarys.contains(diary)){
                            diarys.add(diary!!)
                        }
                    }
                    //use the values to build the view
                    note_adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailPatientActivity, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        //get all subs
        val database_sub = database.child("subscribers")
        database_sub.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(dataSnapShot in snapshot.children){

                        //read each patient with their key in database
                        val subscriber = dataSnapShot.getValue(Subscriber::class.java)
                        if(subscriber!!.canEdit){
                            if(!sub_car.contains(subscriber)){
                                sub_car.add(subscriber!!)
                            }
                        }
                        else if(!sub_mem.contains(subscriber)){
                            sub_mem.add(subscriber!!)
                        }
                    }
                    //use the values to build the view
                    car_adapter.notifyDataSetChanged()
                    fam_adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailPatientActivity, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        val currentUser = auth.currentUser
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val addButton = findViewById<Button>(R.id.addButton)
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

                            if (!subscriber.canEdit) {
                                addButton.isEnabled = false
                                addButton.setVisibility(View.GONE);
                            } else {
                                addButton.setOnClickListener {

                                    val fullName = user.text.toString()


                                    val toAdd = Intent(this@DetailPatientActivity, AddNotesActivity::class.java)
                                    toAdd.putExtra("userToBeUpdated", child_key)
                                    toAdd.putExtra("currentUser", currentUser)
                                    toAdd.putExtra("userNameToBeUpdated", fullName)
                                    startActivity(toAdd)
                                }
                            }
                        }

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