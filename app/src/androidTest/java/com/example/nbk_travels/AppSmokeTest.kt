package com.example.nbk_travels

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Test

class AppSmokeTest {
    @Test fun appLaunchesToSplashScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText("Connecting you to your destination..."))
                .check(matches(isDisplayed()))
        }
    }
}
