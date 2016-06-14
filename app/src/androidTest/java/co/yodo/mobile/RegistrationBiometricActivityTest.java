package co.yodo.mobile;

import android.content.ComponentName;
import android.support.test.espresso.intent.Intents;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.rule.RegistrationBiometricTestRule;
import co.yodo.mobile.ui.CameraActivity;
import co.yodo.mobile.ui.RegistrationBiometricActivity;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Created by hei on 13/06/16.
 * Tests for the registration activity
 */
@RunWith( AndroidJUnit4.class )
@LargeTest
public class RegistrationBiometricActivityTest {
    @Rule
    public RegistrationBiometricTestRule mActivityRule = new RegistrationBiometricTestRule<>( RegistrationBiometricActivity.class );

    @Test
    public void testNoBiometric() throws Exception {
        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        assertEquals( ProgressDialogHelper.getInstance().isProgressDialogShowing(), false );
    }

    @Test
    public void testStartBiometric() throws Exception {
        Intents.init();

        onView( withId( R.id.faceView ) )
                .perform( click() );

        intended( hasComponent( CameraActivity.class.getName() ) );
        Intents.release();
    }
}
