package co.yodo.mobile;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.android.volley.VolleyLog;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.injection.component.ApplicationComponent;
import co.yodo.mobile.injection.component.DaggerApplicationComponent;
import co.yodo.mobile.injection.component.DaggerGraphComponent;
import co.yodo.mobile.injection.component.GraphComponent;
import co.yodo.mobile.injection.module.ApplicationModule;

@ReportsCrashes(formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text
)
public class YodoApplication extends Application {
    /** Component that build the dependencies */
    private static GraphComponent mComponent;

    @Override
    protected void attachBaseContext( Context base ) {
        super.attachBaseContext( base );
        ACRA.init( this );

        // Set DEBUG for Volley
        VolleyLog.DEBUG = AppConfig.DEBUG;
    }

	@Override
    public void onCreate() {
        super.onCreate();

        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule( new ApplicationModule( this ) )
                .build();

        mComponent = DaggerGraphComponent.builder()
                .applicationComponent( appComponent )
                .build();

        registerActivityLifecycleCallbacks( new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated( Activity activity, Bundle savedInstanceState ) {

            }

            @Override
            public void onActivityStarted( Activity activity ) {

            }

            @Override
            public void onActivityResumed( Activity activity ) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
                mNotificationManager.cancel( 0 );
            }

            @Override
            public void onActivityPaused( Activity activity ) {

            }

            @Override
            public void onActivityStopped( Activity activity ) {

            }

            @Override
            public void onActivitySaveInstanceState( Activity activity, Bundle outState ) {

            }

            @Override
            public void onActivityDestroyed( Activity activity ) {

            }
        } );
    }

    public static GraphComponent getComponent() {
        return mComponent;
    }
}
