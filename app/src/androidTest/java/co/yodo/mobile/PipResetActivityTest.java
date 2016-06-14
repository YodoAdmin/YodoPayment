package co.yodo.mobile;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.ui.PipResetActivity;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Created by hei on 14/06/16.
 * Tests for the pip reset activity
 */
@RunWith( AndroidJUnit4.class )
@LargeTest
public class PipResetActivityTest {
    /** Test strings */
    private static final String shortPIP = "abc";
    private static final String newPIP   = "aaaa";
    private static final String wrongPIP = "abcd";

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>( PipResetActivity.class );

    @Test
    public void testPIPInput() throws Exception {
        onView( withId( R.id.currentPipText ) )
                .perform( typeText( shortPIP ), closeSoftKeyboard() );

        onView( withId( R.id.bResetPip ) )
                .perform( click() );

        assertEquals( ProgressDialogHelper.getInstance().isProgressDialogShowing(), false );
    }
}
