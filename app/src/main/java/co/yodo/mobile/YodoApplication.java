package co.yodo.mobile;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "", 
                formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text)

public class YodoApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        //ACRA.init( this );

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
}
