package com.example.project13application.ui.models

import com.google.firebase.database.Exclude
import kotlinx.datetime.LocalDate


/**
 *
 * An model class of Diary with id, dataString and content fields.
 *
 * Author: Wenbo Peng
 * BannerID: B00916504
 * version: 1.0
 */
data class Diary(

    val id: String = "",
    val dateString: String = "",
    val content: String = ""

){
    // get date data from Firebase an convert to localDate type
    @get:Exclude
    val date: LocalDate
        get() = LocalDate.parse(dateString)

    // the date will be set as localDate type, but store it to Firebase as String
    companion object {
        fun create(id: String, date: LocalDate, content: String): Diary {
            return Diary(id, date.toString(), content)
        }
    }
}