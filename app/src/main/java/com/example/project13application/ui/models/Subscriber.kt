package com.example.project13application.ui.models

/**
 *
 * An model class of Subscriber with username, type, canEdit and subscriptionCode fields.
 * Also two categories are enumerated, Caregiver and Family member
 * Author: Wenbo Peng
 * BannerID: B00916504
 * version: 1.0
 */

data class Subscriber(

    val username: String = "",

    val type: SubscriberType = SubscriberType.FAMILY_MEMBER,
    val canEdit: Boolean = false,
    val subscriptionCode:String = "" // <-- New value. Makes it easier to read subs
)

// subscriber types enumeration
enum class SubscriberType {
    FAMILY_MEMBER,
    CAREGIVER
}