package co.yodo.mobile;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.InputType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.rule.ResetPIPTestRule;
import co.yodo.mobile.ui.ResetPIPActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withInputType;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by hei on 14/06/16.
 * Tests for the pip reset activity
 */
@RunWith( AndroidJUnit4.class )
public class ResetPIPActivityTest {
    /** Test strings */
    private static final String correctPIP = "aaaa";
    private static final String shortPIP   = "abc";
    private static final String newPIP     = "aaaa";
    private static final String wrongPIP   = "----";

    @Rule
    public ResetPIPTestRule mActivityRule = new ResetPIPTestRule<>( ResetPIPActivity.class );

    /**
     * Tests when the current PIP is not defined,
     * and the new and confirmation PIP are correct
     * @throws Exception
     */
    @Test
    public void testNoPIPInput() throws Exception {
        onView( withId( R.id.etNewPip ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.etConfirmPip ) )
                .perform( scrollTo(), typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bResetPip ) )
                .perform( click() );

        onView( withText( R.string.pip_short ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the new PIP is too short
     * @throws Exception
     */
    @Test
    public void testNewPIPInput() throws Exception {
        onView( withId( R.id.etNewPip ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bResetPip ) )
                .perform( click() );

        onView( withText( R.string.pip_short ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the confirmation PIP is different from the
     * new PIP or too short
     * @throws Exception
     */
    @Test
    public void testConfirmationPIPInput() throws Exception {
        onView( withId( R.id.etNewPip ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.etConfirmPip ) )
                .perform( scrollTo(), typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bResetPip ) )
                .perform( click() );

        onView( withText( R.string.pip_different ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the new PIP is not present,
     * and the current and confirmation PIP are correctly defined
     * @throws Exception
     */
    @Test
    public void testNoNewPIPInput() throws Exception {
        onView( withId( R.id.etConfirmPip ) )
                .perform( scrollTo(), typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bResetPip ) )
                .perform( click() );

        onView( withText( R.string.pip_short ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when everything is correctly defined,
     * however it is not the user's PIP, so it will
     * return an error in the authentication
     * @throws Exception
     */
    @Test
    public void testCorrectPIPInput() throws Exception {
        onView( withId( R.id.etNewPip ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.etConfirmPip ) )
                .perform( scrollTo(), typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bResetPip ) )
                .perform( click() );

        onView( withText( ServerResponse.ERROR_INCORRECT_PIP ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the user forgot its PIP, and
     * the new PIP is too short
     * @throws Exception
     */
    @Test
    public void testForgotPIPInput() throws Exception {
        onView( withId( R.id.etNewPip ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bForgotPip ) )
                .perform( click() );

        onView( withText( R.string.pip_short ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the user forgot its PIP,
     * and the confirmation PIP is different than the new PIP
     * or too short
     * @throws Exception
     */
    @Test
    public void testForgotConfirmationPIPInput() throws Exception {
        onView( withId( R.id.etNewPip ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.etConfirmPip ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bForgotPip ) )
                .perform( click() );

        onView( withText( R.string.pip_different ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the user forgot its PIP,
     * and everything is correct, but the hardware token is fake
     * so it should show an error
     * @throws Exception
     */
    @Test
    public void testForgotCorrectPIPInput() throws Exception {
         onView( withId( R.id.etNewPip ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.etConfirmPip ) )
                .perform( scrollTo(), typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bForgotPip ) )
                .perform( click() );

        onView( withText( ServerResponse.ERROR_FAILED ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests that all the TextViews are being showed not as
     * password input types
     * @throws Exception
     */
    @Test
    public void testShowPIP() throws Exception {
        /*onView( withId( R.id.cbShowPassword ) )
                .perform( scrollTo(), click() );

        onView( withId( R.id.etNewPip ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );

        onView( withId( R.id.etConfirmPip ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );*/
    }
}
