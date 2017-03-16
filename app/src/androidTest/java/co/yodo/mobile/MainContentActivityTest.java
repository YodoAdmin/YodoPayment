package co.yodo.mobile;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.InputType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.rule.MainTestRule;
import co.yodo.mobile.ui.CouponsActivity;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.SettingsActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withInputType;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * Created by hei on 15/06/16.
 * Test the interactions with the MainActivity
 */
@RunWith( AndroidJUnit4.class )
@LargeTest
public class MainContentActivityTest {
    /** Test strings */
    private static final String shortPIP = "abc";
    private static final String wrongPIP = "----";

    @Rule
    public MainTestRule mActivityRule = new MainTestRule<>( MainActivity.class );

    /**
     * Tests when the user inserts a short PIP to
     * generate a SKS
     * @throws Exception
     */
    @Test
    public void testPaymentPIPInput() throws Exception {
        // Press to make the input PIP appears
        onView( withId( R.id.image_payment ) )
                .perform( click() );

        // Check if the dialog appeared
        /*onView( withText( R.string.input_pip ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );*/

        // Insert a short PIP in the EditText
        onView( withClassName( endsWith( "EditText" ) ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        // Press show password
        /*onView( withId( R.id.showPassword ) )
                .perform( click() );*/

        // Verify the type of the EditText after show password pressed
        onView( withClassName( endsWith( "EditText" ) ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );

        // Press Ok button to go
        onView( withId( android.R.id.button1 ) )
                .perform( click() );

        // Cancel the dialog
        onView( withId( android.R.id.button2 ) )
                .perform( click() );

        // There should be an error for the PIP
        onView( withText( R.string.error_pip_length ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );

        // The dialog should not be visible any more
        /*onView( withText( R.string.input_pip ) )
                .check( doesNotExist() );*/
    }

    /**
     * Tests when the user inserts a PIP with correct size to
     * generate a SKS
     * @throws Exception
     */
    @Test
    public void testPaymentCorrectPIPInput() throws Exception {
        // Press to make the input PIP appears
        onView( withId( R.id.image_payment ) )
                .perform( click() );

        // Check if the dialog appeared
        /*onView( withText( R.string.input_pip ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );*/

        // Insert a correct PIP in the EditText
        onView( withClassName( endsWith( "EditText" ) ) )
                .perform( typeText( wrongPIP ), closeSoftKeyboard() );

        // Press show password
        /*onView( withId( R.id.showPassword ) )
                .perform( click() );*/

        // Verify the type of the EditText after show password pressed
        onView( withClassName( endsWith( "EditText" ) ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );

        // Press Ok button to go
        onView( withId( android.R.id.button1 ) )
                .perform( click() );

        // Verify that the correct error appeared
        onView( withText( ServerResponse.ERROR_INCORRECT_PIP ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the user wants to open the coupons
     * activity
     * @throws Exception
     */
    @Test
    public void testCoupon() throws Exception {
        Intents.init();

        onView( withId( R.id.image_coupons ) )
                .perform( click() );

        intended( hasComponent( CouponsActivity.class.getName() ) );
        Intents.release();
    }

    /**
     * Tests when the user wants to open the network
     * activity - not yet implemented
     * @throws Exception
     */
    @Test
    public void testNetwork() throws Exception {
        onView( withId( R.id.image_social ) )
                .perform( click() );

        onView( allOf( withId( android.support.design.R.id.snackbar_text ), withText( R.string.error_available ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests when the user wants to open the settings
     * activity
     * @throws Exception
     */
    @Test
    public void testSettings() throws Exception {
        Intents.init();

        openActionBarOverflowOrOptionsMenu( InstrumentationRegistry.getTargetContext() );

        onView( withText( R.string.action_settings ) )
                .perform( click() );

        intended( hasComponent( SettingsActivity.class.getName() ) );
        Intents.release();
    }

    /**
     * Tests when the user wants to open the about
     * dialog
     * @throws Exception
     */
    @Test
    public void testAbout() throws Exception {
        openActionBarOverflowOrOptionsMenu( InstrumentationRegistry.getTargetContext() );

       onView( withText( R.string.action_about ) )
                .perform( click() );

        // Verify that the correct error appeared
        onView( withText( R.string.action_about ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );

        // Press Ok button to go
        onView( withId( android.R.id.button1 ) )
                .perform( click() );

        // The dialog should not be visible any more
        onView( withText( R.string.action_about ) )
                .check( doesNotExist() );
    }
}
