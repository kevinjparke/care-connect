package com.example.project13application.ui.models

import com.example.project13application.ui.models.Subscriber

/**
 *
 * An model class of Patient with id, firstName, lastName and subscriptionCode fields.
 *
 * Author: Wenbo Peng
 * BannerID: B00916504
 * version: 2.0
 */

data class Patient(

    // automatic generate by Firebase
    var id: String = "",
    var username: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var subscriptionCode: String = "",

    // one patient can have multiple diaries
    val diary: List<Diary>? = null,

    // one patient can have multiple subscribers
    // and we have two types of subscriber
    val familyMembers: List<Subscriber>? = null,
    val caregivers: List<Subscriber>? = null



)  {
    init {
        // Set the subscription code as the last 8 characters (capitalized) of the id property
        if (id.isNotBlank()) {
            subscriptionCode = id.takeLast(8).uppercase()
        }
    }
}