package co.yodo.mobile.rule;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.RegistrationBiometricActivity;

/**
 * Created by hei on 13/06/16.
 * Test rule for the registration biometric activity
 */
public class RegistrationBiometricTestRule<A extends RegistrationBiometricActivity> extends ActivityTestRule<A> {
    /** Test context */
    private static Context mCtx;

    /** Mock values */
    private static final String fAuthNumber = "fake_authnumber";

    public RegistrationBiometricTestRule( Class<A> activityClass ) {
        super( activityClass );
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        mCtx = InstrumentationRegistry.getTargetContext();
    }

    @Override
    protected Intent getActivityIntent() {
        Intent customIntent = new Intent();
        customIntent.putExtra( Intents.AUTH_NUMBER, fAuthNumber );
        return customIntent;
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        PrefUtils.clearPrefConfig( mCtx );
    }
}
