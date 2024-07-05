package com.arfian.story

import android.view.KeyEvent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.arfian.story.utils.EspressoIdlingResource
import com.arfian.story.view.login.LoginActivity
import com.arfian.story.view.story.home.HomeStoryActivity
import com.arfian.story.view.welcome.WelcomeActivity
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LoginAndLogoutTest {

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun test01loginSuccessfullyNavigatesToHomeStoryActivity() {
        Intents.init()
        onView(withId(R.id.emailEditText)).perform(typeText("dicodingtest01@gmail.com"))
        onView(withId(R.id.emailEditText)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withId(R.id.passwordEditText)).perform(typeText("DICODING"))
        onView(withId(R.id.emailEditText)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withId(R.id.loginButton)).perform(click())
        intended(hasComponent(HomeStoryActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun test02logoutSuccessfullyNavigatesToWelcomeActivity() {
        Intents.init()
        ActivityScenario.launch(HomeStoryActivity::class.java)
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.action_logout)).perform(click())
        intended(hasComponent(WelcomeActivity::class.java.name), times(2))
        Intents.release()
    }
}