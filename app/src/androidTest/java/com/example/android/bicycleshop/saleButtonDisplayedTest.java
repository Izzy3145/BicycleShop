package com.example.android.bicycleshop;

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class saleButtonDisplayedTest {
    //determine where the test should be run
    @Rule
    public ActivityTestRule<CatalogueActivity> activityTestRule =
            new ActivityTestRule<>(CatalogueActivity.class);
    @Test
    public void TestSaleButtonPresence(){
        //1) ViewMatcher, ViewAction and ViewAssertion
        onView(withId(R.id.sale_button)).check(matches(isDisplayed()));
    }
}
