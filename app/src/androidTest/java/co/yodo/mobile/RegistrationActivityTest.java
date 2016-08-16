package co.yodo.mobile;

import android.support.test.runner.AndroidJUnit4;
import android.text.InputType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.rule.RegistrationTestRule;
import co.yodo.mobile.ui.RegistrationActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withInputType;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by hei on 13/06/16.
 * Tests for the registration activity
 */
@RunWith( AndroidJUnit4.class )
public class RegistrationActivityTest  {
    /** Test strings */
    private static final String shortPIP = "abc";
    private static final String newPIP   = "aaaa";
    private static final String wrongPIP = "----";

    @Rule
    public RegistrationTestRule mActivityRule = new RegistrationTestRule<>( RegistrationActivity.class );

    /**
     * Tests when the user inserts a short PIP
     * @throws Exception
     */
    @Test
    public void testPIPInput() throws Exception {
        onView( withId( R.id.etPip ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        onView( withText( R.string.pip_short ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the PIP is not present
     * @throws Exception
     */
    @Test
    public void testNoPIP() throws Exception {
        onView( withId( R.id.etConfirmPip ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        onView( withText( R.string.pip_short ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the confirmation PIP is different than the
     * PIP
     * @throws Exception
     */
    @Test
    public void testConfirmationPIPInput() throws Exception {
        onView( withId( R.id.etPip ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.etConfirmPip ) )
                .perform( typeText( wrongPIP ), closeSoftKeyboard() );

        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        onView( withText( R.string.pip_different ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests that all the TextViews are being showed not as
     * password input types
     * @throws Exception
     */
    @Test
    public void testShowPassword() throws Exception {
        onView( withId( R.id.cbShowPassword ) )
                .check( matches( not( isChecked() ) ) )
                .perform( click() );

        onView( withId( R.id.etPip ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );

        onView( withId( R.id.etConfirmPip ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );
    }
}
