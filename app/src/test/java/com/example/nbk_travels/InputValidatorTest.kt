package com.example.nbk_travels

import com.example.nbk_travels.util.InputValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun password_isTooShort_returnsFalse() {
        assertFalse(InputValidator.isValidPassword("12345"))
    }

    @Test
    fun password_isLongEnough_returnsTrue() {
        assertTrue(InputValidator.isValidPassword("123456"))
    }
}
