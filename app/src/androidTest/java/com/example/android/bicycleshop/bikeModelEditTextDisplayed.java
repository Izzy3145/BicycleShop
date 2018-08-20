package com.example.android.bicycleshop;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)

public class bikeModelEditTextDisplayed {
    //determine where the test should be run
    @Rule
    public ActivityTestRule<EditorActivity> activityTestRule =
            new ActivityTestRule<>(EditorActivity.class);

    @Test
    public void testBikeModelEditText (){
        onView(withId(R.id.model)).check(matches(isDisplayed()));
        onView(withId(R.id.model)).perform(typeText("Dawes Galaxy"));
    }
}
