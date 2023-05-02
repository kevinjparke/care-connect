package com.example.project13application


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project13application.ui.models.Diary

/**
 *
 * This Class is used to get the Diary
 * entry of each patient on Firebase,
 * and adapter will pass these diaries to local.
 *
 * Author: Rui Zeng
 * BannerID: B00800727
 * version: 1.0
 */

class DiaryAdapter(val diarys:ArrayList<Diary>): RecyclerView.Adapter<DiaryAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val date: TextView = itemView.findViewById(R.id.note_date)
        val context: TextView = itemView.findViewById(R.id.note_diary)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryAdapter.MyViewHolder {
        val dairyView = LayoutInflater.from(parent.context).inflate(R.layout.patient_notes,parent,false)
        return DiaryAdapter.MyViewHolder(dairyView)
    }

    override fun getItemCount(): Int {
        return diarys.size
    }
    //link to the patient_note.xml as elements
    override fun onBindViewHolder(holder: DiaryAdapter.MyViewHolder, position: Int) {
        holder.date.text = diarys[position].dateString
        holder.context.text = diarys[position].content
        }
}