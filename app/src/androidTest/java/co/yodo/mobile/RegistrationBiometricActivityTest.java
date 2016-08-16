package co.yodo.mobile;

import android.support.test.espresso.intent.Intents;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yodo.mobile.rule.RegistrationBiometricTestRule;
import co.yodo.mobile.ui.CameraActivity;
import co.yodo.mobile.ui.RegistrationBiometricActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by hei on 13/06/16.
 * Tests for the registration activity
 */
@RunWith( AndroidJUnit4.class )
public class RegistrationBiometricActivityTest {
    @Rule
    public RegistrationBiometricTestRule mActivityRule = new RegistrationBiometricTestRule<>( RegistrationBiometricActivity.class );

    /**
     * Tests when the biometric token is not yet set
     * @throws Exception
     */
    @Test
    public void testNoBiometric() throws Exception {
        onView( withId( R.id.registerPipButton ) )
                .perform( click() );

        onView( withText( R.string.face_required ) )
                .inRoot( withDecorView( not( mActivityRule.getActivity().getWindow().getDecorView() ) ) )
                .check( matches( isDisplayed() ) );
    }

    /**
     * Tests that the CameraActivity is started after the user
     * pressed the Biometric Token button
     * @throws Exception
     */
    @Test
    public void testStartBiometric() throws Exception {
        Intents.init();

        onView( withId( R.id.ivFaceBiometric ) )
                .perform( click() );

        intended( hasComponent( CameraActivity.class.getName() ) );
        Intents.release();
    }
}
