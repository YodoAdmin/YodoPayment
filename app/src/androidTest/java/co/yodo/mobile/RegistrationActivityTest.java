package co.yodo.mobile;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.InputType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.rule.RegistrationTestRule;
import co.yodo.mobile.ui.RegistrationActivity;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withInputType;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

/**
 * Created by hei on 13/06/16.
 * Tests for the registration activity
 */
@RunWith( AndroidJUnit4.class )
@LargeTest
public class RegistrationActivityTest  {
    /** Test strings */
    private static final String shortPIP = "abc";
    private static final String newPIP   = "aaaa";
    private static final String wrongPIP = "abcd";

    @Rule
    public RegistrationTestRule mActivityRule = new RegistrationTestRule<>( RegistrationActivity.class );

    @Test
    public void testPIPInput() throws Exception {
        onView( withId( R.id.pipText ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        assertEquals( ProgressDialogHelper.getInstance().isProgressDialogShowing(), false );
    }

    @Test
    public void testConfPIPInput() throws Exception {
        onView( withId( R.id.confirmationPipText ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        assertEquals( ProgressDialogHelper.getInstance().isProgressDialogShowing(), false );
    }

    @Test
    public void testMatchPIP() throws Exception {
        onView( withId( R.id.pipText ) )
                .perform( typeText( newPIP ), closeSoftKeyboard() );

        onView( withId( R.id.confirmationPipText ) )
                .perform( typeText( wrongPIP ), closeSoftKeyboard() );

        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        assertEquals( ProgressDialogHelper.getInstance().isProgressDialogShowing(), false );
    }

    @Test
    public void testShowPassword() throws Exception {
        onView( withId( R.id.cbShowPassword ) )
                .check( matches( not( isChecked() ) ) )
                .perform( click() );

        onView( withId( R.id.pipText ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );

        onView( withId( R.id.confirmationPipText ) )
                .check( matches( withInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD ) ) );
    }
}
