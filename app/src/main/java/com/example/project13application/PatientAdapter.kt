package com.example.project13application

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project13application.ui.models.Patient

/**
 *
 * This Class is used to get the Patient
 * details on Firebase, and adapter will
 * pass these patient details data to local.
 *
 * Author: Rui Zeng
 * BannerID: B00800727
 * version: 1.0
 */
class PatientAdapter(val onViewDetailClickListener: OnViewDetailClickListener, val patients:ArrayList<Patient>, val keys:ArrayList<String>):RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val patientFN:TextView = itemView.findViewById(R.id.fn_patient)
        val key:TextView = itemView.findViewById(R.id.key_patient)
        val btn: Button = itemView.findViewById(R.id.btn_detail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val patientView = LayoutInflater.from(parent.context).inflate(R.layout.patient_list,parent,false)
        return MyViewHolder(patientView)
    }

    override fun getItemCount(): Int {
        return patients.size
    }
    //link to the patient_list.xml as elements
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // set patient info
        holder.patientFN.text = "${patients[position].firstName} ${patients[position].lastName}"
        holder.key.text = keys[position]

        Log.d("PatientAdapter", "Patient Key: ${keys[position]}")

        // view detail button
        val context = holder.btn.context
        holder.btn.setOnClickListener {
            onViewDetailClickListener.onViewDetailClick(keys[position])
        }
    }

    interface OnViewDetailClickListener {
        fun onViewDetailClick(patientId: String)
    }

}