package com.innerCat.pillBox.activities;


import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.innerCat.pillBox.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {
        ViewInteraction extendedFloatingActionButton = onView(
                allOf(withId(R.id.fab), withText("Add Item"),
                        childAtPosition(
                                allOf(withId(R.id.coordinatorLayout),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        extendedFloatingActionButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editName),
                        childAtPosition(
                                allOf(withId(R.id.toolbar_layout),
                                        childAtPosition(
                                                withId(R.id.app_bar),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("X"), closeSoftKeyboard());

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.okButton),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                allOf(withId(R.id.toolbar_layout), withContentDescription("X")),
                                                1)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.refillButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editRefill),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.custom),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("1"), closeSoftKeyboard());

        ViewInteraction materialButton = onView(
                allOf(withId(android.R.id.button1), withText("Ok"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        materialButton.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.stockTV), withText("1"),
                        withParent(withParent(withId(R.id.cardView))),
                        isDisplayed()));
        textView.check(matches(withText("1")));

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.rvItems),
                        childAtPosition(
                                withId(R.id.nestedScrollView),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(0, longClick()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.titleTV), withText("Undated Refills"),
                        withParent(withParent(withId(R.id.rvRefills))),
                        isDisplayed()));
        textView2.check(matches(withText("Undated Refills")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.amountTV), withText("1"),
                        withParent(withParent(withId(R.id.refillItemCardView))),
                        isDisplayed()));
        textView3.check(matches(withText("1")));

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.editButton),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                allOf(withId(R.id.toolbar_layout), withContentDescription("X")),
                                                1)),
                                1),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.rvRefills),
                        childAtPosition(
                                withId(R.id.constraintLayout),
                                1)));
        recyclerView2.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editRefill), withText("1"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.custom),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("2"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.editRefill), withText("2"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.custom),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());

        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.expiryButton), withText("Set Expiry"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.custom),
                                        0),
                                1),
                        isDisplayed()));
        materialButton2.perform(click());

        ViewInteraction materialButton3 = onView(
                allOf(withId(R.id.confirm_button), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.date_picker_actions),
                                        childAtPosition(
                                                withId(R.id.mtrl_calendar_main_pane),
                                                1)),
                                1),
                        isDisplayed()));
        materialButton3.perform(click());

        ViewInteraction materialButton4 = onView(
                allOf(withId(android.R.id.button1), withText("Ok"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        materialButton4.perform(scrollTo(), click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.amountTV), withText("2"),
                        withParent(withParent(withId(R.id.refillItemCardView))),
                        isDisplayed()));
        textView4.check(matches(withText("2")));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.dateTV), withText("2021-05-21"),
                        withParent(withParent(withId(R.id.refillItemCardView))),
                        isDisplayed()));
        textView5.check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.backButton),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                allOf(withId(R.id.toolbar_layout), withContentDescription("X")),
                                                1)),
                                0),
                        isDisplayed()));
        appCompatImageButton4.perform(click());

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.expiryTV), withText("2 expiring today"),
                        withParent(withParent(withId(R.id.cardView))),
                        isDisplayed()));
        textView6.check(matches(withText("2 expiring today")));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.stockTV), withText("2"),
                        withParent(withParent(withId(R.id.cardView))),
                        isDisplayed()));
        textView7.check(matches(withText("2")));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position ) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo( Description description ) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely( View view ) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
