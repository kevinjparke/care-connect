package com.example.project13application.utilities

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test


class FormValidationTest {

    @Test
    fun testIsValidatePassword() {
        assertTrue(FormValidator.isValidatePassword("Password123"))
        assertFalse(FormValidator.isValidatePassword("password"))
        assertFalse(FormValidator.isValidatePassword("Password"))
    }

    @Test
    fun testIsValidEmailAddress() {
        assertTrue(FormValidator.isValidEmailAddress("example@example.com"))
        assertFalse(FormValidator.isValidEmailAddress("example.com"))
        assertFalse(FormValidator.isValidEmailAddress("example@.com"))
        assertFalse(FormValidator.isValidEmailAddress("example@com."))
    }

    @Test
    fun testIsPasswordMatched() {
        assertTrue(FormValidator.isPasswordMatched("password", "password"))
        assertFalse(FormValidator.isPasswordMatched("password", "Password"))
        assertFalse(FormValidator.isPasswordMatched("password", "password123"))
    }

    @Test
    fun testIsFormComplete() {
        assertTrue(FormValidator.isFormComplete("example@example.com", "password", "password"))
        assertFalse(FormValidator.isFormComplete("", "password", "password"))
        assertFalse(FormValidator.isFormComplete("example@example.com", "", "password"))
        assertFalse(FormValidator.isFormComplete("example@example.com", "password", ""))
    }
}