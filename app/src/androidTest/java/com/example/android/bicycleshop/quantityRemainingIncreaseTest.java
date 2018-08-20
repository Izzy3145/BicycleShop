package com.example.android.bicycleshop;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class quantityRemainingIncreaseTest {
    @Rule
    public ActivityTestRule<EditorActivity> activityTestRule =
            new ActivityTestRule<>(EditorActivity.class);

    @Test
    public void quantityIncreaseFromZeroToOne(){
        //viewMatcher and ViewAction
        onView(withId(R.id.plus_one_stock)).perform(click());
        //ViewAssertion
        onView(withId(R.id.quantity)).check(matches(withText("1")));
    }
}
