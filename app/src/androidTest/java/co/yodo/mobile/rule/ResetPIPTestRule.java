package co.yodo.mobile.rule;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.ResetPIPActivity;

/**
 * Created by hei on 14/06/16.
 * Test rule for the ResetPIP activity
 */
public class ResetPIPTestRule<A extends ResetPIPActivity> extends ActivityTestRule<A> {
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
        PrefUtils.clearPrefConfig( mCtx );

        // Set preferences
        PrefUtils.saveHardwareToken( mCtx, fHardwareToken );
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        PrefUtils.clearPrefConfig( mCtx );
    }
}
