package com.example.project13application

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.project13application.ui.models.Diary
import com.example.project13application.ui.models.Patient
import com.example.project13application.ui.models.Subscriber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.util.*

/**
 *
 * This class is used to add new daily notes for patients.
 * Family members cannot access this page, only Caregiver can.
 * The page will display information about patient and the current subscriber.
 *
 * Author: Meet Kumer Patel
 * BannerID: B00857644
 * version: 2.0
 */
class AddNotesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val userToBeUpdated = intent.getStringExtra("userToBeUpdated").toString()
        val currentUser = intent.getStringExtra("currentUser").toString()
        val fullName = intent.getStringExtra("userNameToBeUpdated").toString()

        //val userToBeUpdatedTV = findViewById<TextView>(R.id.addingNotesFor)

        //userToBeUpdatedTV.setText(fullName)

        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            // Fetch current user's username
            val subscribersRef = database.getReference("subscribers")
            subscribersRef.child(currentUserUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val subscriber = snapshot.getValue(Subscriber::class.java)
                        if (subscriber != null) {
                            val currentUserTV = findViewById<TextView>(R.id.currentUserAddNotes)
                            currentUserTV.text = subscriber.username
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AddNotes", "Failed to fetch current user's username: ${error.message}")
                }
            })
        }

        val patientRef = database.getReference("patients").child(userToBeUpdated)
        patientRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val patient = snapshot.getValue(Patient::class.java)
                    if (patient != null) {
                        val userToBeUpdatedTV = findViewById<TextView>(R.id.addingNotesFor)
                        userToBeUpdatedTV.text = fullName
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddNotes", "Failed to fetch patient data: ${error.message}")
            }
        })

        val addButton = findViewById<Button>(R.id.addNotesButton)
        addButton.setOnClickListener {
            val addNotesML = findViewById<EditText>(R.id.addNotesML)
            val content = addNotesML.text.toString()

            val todayDate = LocalDate.now().toString()
            val d : Diary = Diary(id = "", dateString = todayDate, content = content)

            //firebase initial
            val diaryId = patientRef.child(userToBeUpdated).child("diary entry").push().key ?: ""
            val diaryRef = patientRef.child("diary entry")
            diaryRef.child(diaryId).setValue(d).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Add Successful", Toast.LENGTH_SHORT).show()

                    val toDetails = Intent(this, DetailPatientActivity::class.java)
                    toDetails.putExtra("patientId", userToBeUpdated)
                    this.startActivity(toDetails)
                } else {
                    Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
                }
            }
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