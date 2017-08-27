package co.yodo.mobile.rule;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.ui.ResetPipActivity;

/**
 * Created by hei on 14/06/16.
 * Test rule for the ResetPIP activity
 */
public class ResetPIPTestRule<A extends ResetPipActivity> extends ActivityTestRule<A> {
    /** Test context */
    private static Context mCtx;

    /** Mock values */
    private static final String fHardwareToken = "fake_hardware_token";

    public ResetPIPTestRule( Class<A> activityClass ) {
        super( activityClass );
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        mCtx = InstrumentationRegistry.getTargetContext();
        PreferencesHelper.clearPrefConfig(mCtx);

        // Set preferences
        PreferencesHelper.saveHardwareToken( fHardwareToken );
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        PreferencesHelper.clearPrefConfig(mCtx);
    }
}
