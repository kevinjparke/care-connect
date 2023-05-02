package com.example.project13application.utilities

object FormValidator {
    fun isValidatePassword(password: String): Boolean {
        val capitalLetterRegex = Regex("[A-Z]")
        val numberRegex = Regex("\\d")
        return password.length >= 8 &&
                capitalLetterRegex.containsMatchIn(password) &&
                numberRegex.containsMatchIn(password)
    }

    fun isValidEmailAddress(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return emailRegex.matches(email)
    }

    fun isPasswordMatched(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isFormComplete(email: String, password: String, confirmPassword: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
    }
}