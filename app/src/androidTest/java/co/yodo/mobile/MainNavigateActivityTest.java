package co.yodo.mobile;

import android.support.test.espresso.intent.Intents;
import android.support.test.runner.AndroidJUnit4;
import android.text.InputType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.rule.MainTestRule;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.ReceiptsActivity;
import co.yodo.mobile.ui.ResetPipActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withInputType;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * Created by hei on 15/06/16.
 * Test the interactions with the navigation bar of the MainActivity
 */
@RunWith( AndroidJUnit4.class )
public class MainNavigateActivityTest {
    /** Test strings */
    private static final String shortPIP = "abc";
    private static final String wrongPIP = "----";
    private static final String linkingCode = "linkingCode";

    @Rule
    public MainTestRule mActivityRule = new MainTestRule<>( MainActivity.class );

    /**
     * Tests when the user clicks in ResetPIP
     * @throws Exception
     */
    @Test
    public void testResetPIP() throws Exception {
        Intents.init();

        onView( withId( R.id.layout_payment ) )
                .perform( open() );

        onView( withId( R.id.button_reset_pip ) )
                .perform( click() );

        intended( hasComponent( ResetPipActivity.class.getName() ) );
        Intents.release();
    }

    /**
     * Tests when the user clicks in Receipts
     * @throws Exception
     */
    @Test
    public void testReceipts() throws Exception {
        Intents.init();

        onView( withId( R.id.layout_payment ) )
                .perform( open() );

        onView( withId( R.id.button_receipts ) )
                .perform( click() );

        intended( hasComponent( ReceiptsActivity.class.getName() ) );
        Intents.release();
    }

    /**
     * Tests when the user clicks in links
     * @throws Exception
     */
    @Test
    public void testLinks() throws Exception {
        // Open the links menu
        openLinks();

        // Cancel the dialog
        onView( withId( android.R.id.button2 ) )
                .perform( click() );

        // The dialog should not be visible any more
        onView( withText( R.string.text_options_select ) )
                .check( doesNotExist() );
    }

    /**
     * Tests when the user inserts a short PIP to
     * generate a linking code
     * @throws Exception
     */
    @Test
    public void testLinkCodePIPInput() throws Exception {
        // Open the links menu
        openLinks();

        // Click on Links
        onData( anything() )
                .atPosition( 0 )
                .perform( click() );

        verifyShortPIP();
    }

    /**
     * Tests when the user inserts a short PIP to
     * generate a linking code
     * @throws Exception
     */
    @Test
    public void testLinkCodeCorrectPIPInput() throws Exception {
        // Open the links menu
        openLinks();

        // Click on Links
        onData( anything() )
                .atPosition( 0 )
                .perform( click() );

        verifyCorrectPIP( ServerResponse.ERROR_FAILED );
    }

    /**
     * Tests when the user inserts a short PIP to
     * generate a linking code
     * @throws Exception
     */
    @Test
    public void testLinkAccount() throws Exception {
        // Open the links menu
        openLinks();

        // Click on Links
        onData( anything() )
                .atPosition( 1 )
                .perform( click() );

        // Check if the dialog appeared
        onView( withText( R.string.text_linking_code_hint ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );

        // Press Ok button to go
        onView( withId( android.R.id.button2 ) )
                .perform( click() );

        // Open the links menu
        openLinks();

        // Click on Links
        onData( anything() )
                .atPosition( 1 )
                .perform( click() );

        // Insert a short PIP in the EditText
        onView( withClassName( endsWith( "EditText" ) ) )
                .perform( typeText( linkingCode ), closeSoftKeyboard() );

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
        onView( withText( ServerResponse.ERROR_FAILED ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );

        // Press Ok button to go
        onView( withId( android.R.id.button1 ) )
                .perform( click() );

        // Verify that the correct error appeared
        onView( withText( ServerResponse.ERROR_FAILED ) )
                .check( doesNotExist() );
    }

    /**
     * Tests when the user inserts a short PIP to
     * delink accounts
     * @throws Exception
     */
    @Test
    public void testDeLinkAccountPIPInput() throws Exception {
        // Open the links menu
        openLinks();

        // Click on Links
        onData( anything() )
                .atPosition( 2 )
                .perform( click() );

        verifyShortPIP();
    }

    /**
     * Tests when the user inserts a short PIP to
     * delink accounts
     * @throws Exception
     */
    @Test
    public void testDeLinkAccountCorrectPIPInput() throws Exception {
        // Open the links menu
        openLinks();

        // Click on Links
        onData( anything() )
                .atPosition( 2 )
                .perform( click() );

        verifyCorrectPIP( ServerResponse.ERROR_FAILED );
    }

    /**
     * Tests when the user inserts a short PIP to
     * get the balance
     * @throws Exception
     */
    @Test
    public void testBalancePIPInput() throws Exception {
        // Open navigation
        onView( withId( R.id.layout_payment ) )
                .perform( open() );

        // Click on Get Balance
        onView( withId( R.id.button_balance ) )
                .perform( click() );

        verifyShortPIP();
    }

    /**
     * Tests when the user inserts a PIP with correct size to
     * get the balance
     * @throws Exception
     */
    @Test
    public void testBalanceCorrectPIPInput() throws Exception {
        // Open navigation
        onView( withId( R.id.layout_payment ) )
                .perform( open() );

        // Click on Get Balance
        onView( withId( R.id.button_balance ) )
                .perform( click() );

        verifyCorrectPIP( ServerResponse.ERROR_INCORRECT_PIP );
    }

    /**
     * Tests when the user inserts a short PIP to
     * close the account
     * @throws Exception
     */
    @Test
    public void testClosePIPInput() throws Exception {
        // Open navigation
        onView( withId( R.id.layout_payment ) )
                .perform( open() );

        // Click on Close Account
        onView( withId( R.id.button_close_account ) )
                .perform( click() );

        verifyShortPIP();
    }

    /**
     * Tests when the user inserts a PIP with correct size to
     * close the account
     * @throws Exception
     */
    @Test
    public void testCloseCorrectPIPInput() throws Exception {
        // Open navigation
        onView( withId( R.id.layout_payment ) )
                .perform( open() );

        // Click on Close Account
        onView( withId( R.id.button_close_account ) )
                .perform( click() );

        verifyCorrectPIP( ServerResponse.ERROR_INCORRECT_PIP );
    }

    /**
     * Open the links menu
     */
    private void openLinks() {
        // Open navigation
        onView( withId( R.id.layout_payment ) )
                .perform( open() );

        // Click on Links
        onView( withId( R.id.button_links ) )
                .perform( click() );

        // Check if the dialog appeared
        onView( withText( R.string.text_options_select ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests short PIP for any dialog
     */
    private void verifyShortPIP()  {
        // Check if the dialog appeared
        onView( withText( R.string.input_pip ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );

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
        onView( withText( R.string.input_pip ) )
                .check( doesNotExist() );
    }

    /**
     * Tests a correct size PIP for any dialog
     */
    private void verifyCorrectPIP( String error ) {
        // Check if the dialog appeared
        onView( withText( R.string.input_pip ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );

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
        onView( withText( error ) )
                .inRoot( isDialog() )
                .check( matches( isDisplayed() ) );

        // Press Ok button to go
        onView( withId( android.R.id.button1 ) )
                .perform( click() );

        // Verify that the correct error appeared
        onView( withText( ServerResponse.ERROR_FAILED ) )
                .check( doesNotExist() );
    }
}
