package co.yodo.mobile.rule;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.MainActivity;

/**
 * Created by hei on 15/06/16.
 * Test rules for the main activity
 */
public class MainTestRule<A extends MainActivity> extends ActivityTestRule<A> {
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
        PrefUtils.clearPrefConfig( mCtx );

        // Set preferences
        PrefUtils.saveEulaAccepted( mCtx, true );
        PrefUtils.saveFirstLogin( mCtx, false );
        PrefUtils.saveHardwareToken( mCtx, fHardwareToken );
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        PrefUtils.clearPrefConfig( mCtx );
    }
}
