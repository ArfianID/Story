package com.arfian.story

import android.content.Intent
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
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.ActivityTestRule
import com.arfian.story.utils.EspressoIdlingResource
import com.arfian.story.view.login.LoginActivity
import com.arfian.story.view.story.home.HomeStoryActivity
import com.arfian.story.view.welcome.WelcomeActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class LoginAndLogoutTest {

    @Rule
    @JvmField
    var loginActivityRule: ActivityTestRule<LoginActivity> = ActivityTestRule(LoginActivity::class.java, true, false)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        MockitoAnnotations.initMocks(this)
        Intents.init()
    }

    @Test
    fun login_successfullyNavigatesToHomeStoryActivity() {
        onView(withId(R.id.emailEditText)).perform(typeText("dicodingtest01@gmail.com"))
        onView(withId(R.id.emailEditText)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withId(R.id.passwordEditText)).perform(typeText("DICODING"))
        onView(withId(R.id.emailEditText)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withId(R.id.loginButton)).perform(click())
        intended(hasComponent(HomeStoryActivity::class.java.name))
    }

    @Test
    fun testLogout() {
//        ActivityScenario.launch(HomeStoryActivity::class.java)
        loginActivityRule.launchActivity(Intent())
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.action_logout)).perform(click())
        intended(hasComponent(WelcomeActivity::class.java.name))
        Intents.release()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }
}