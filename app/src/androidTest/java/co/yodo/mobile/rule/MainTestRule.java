package co.yodo.mobile.rule;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.ui.PaymentActivity;

/**
 * Created by hei on 15/06/16.
 * Test rules for the main activity
 */
public class MainTestRule<A extends PaymentActivity> extends ActivityTestRule<A> {
    /** Test context */
    private static Context mCtx;

    /** Mock values */
    private static final String fHardwareToken = "fake_hardware_token";

    public MainTestRule( Class<A> activityClass ) {
        super( activityClass );
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        mCtx = InstrumentationRegistry.getTargetContext();
        PreferencesHelper.clearPrefConfig(mCtx);

        // Set preferences
        PreferencesHelper.saveEulaAccepted( true );
        PreferencesHelper.saveFirstLogin( mCtx, false );
        PreferencesHelper.saveHardwareToken( fHardwareToken );
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        PreferencesHelper.clearPrefConfig(mCtx);
    }
}
