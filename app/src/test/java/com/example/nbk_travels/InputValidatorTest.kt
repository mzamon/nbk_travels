package com.example.nbk_travels

import com.example.nbk_travels.util.InputValidator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputValidatorTest {
    @Test fun acceptsValidEmail() {
        assertNull(InputValidator.validateEmail("customer@example.co.za"))
    }

    @Test fun rejectsMalformedEmail() {
        assertEquals("Enter a valid email address.", InputValidator.validateEmail("customer-at-example"))
    }

    @Test fun rejectsShortPassword() {
        assertEquals("Password must be at least 8 characters.", InputValidator.validatePassword("12345"))
    }

    @Test fun rejectsMismatchedPasswords() {
        assertEquals("Passwords do not match.", InputValidator.validateConfirmPassword("password1", "password2"))
    }

    @Test fun rejectsSamePickupAndDropoff() {
        assertEquals(
            "Pickup and destination must be different.",
            InputValidator.validateBooking("Sandton", " sandton ", LocalDate.now().plusDays(1).toString(), "09:30", 2, "450")
        )
    }

    @Test fun rejectsPastTravelDate() {
        assertEquals(
            "Travel date cannot be in the past.",
            InputValidator.validateBooking("Sandton", "OR Tambo", LocalDate.now().minusDays(1).toString(), "09:30", 2, "450")
        )
    }

    @Test fun rejectsInvalidPassengerCount() {
        assertEquals(
            "Passenger count must be between 1 and 50.",
            InputValidator.validateBooking("Sandton", "OR Tambo", LocalDate.now().plusDays(1).toString(), "09:30", 0, "450")
        )
    }

    @Test fun acceptsValidBooking() {
        assertNull(
            InputValidator.validateBooking("Sandton", "OR Tambo", LocalDate.now().plusDays(1).toString(), "09:30", 2, "450.00")
        )
    }
}
