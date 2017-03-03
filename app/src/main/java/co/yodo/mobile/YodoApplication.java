package co.yodo.mobile;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import co.yodo.mobile.injection.component.ApplicationComponent;
import co.yodo.mobile.injection.component.DaggerApplicationComponent;
import co.yodo.mobile.injection.component.DaggerGraphComponent;
import co.yodo.mobile.injection.component.GraphComponent;
import co.yodo.mobile.injection.module.ApiClientModule;
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
    /** Switch server IP address */
    private static final String PROD_IP  = "http://50.56.180.133";   // Production
    //private static final String DEMO_IP  = "http://198.101.209.120"; // Demo
    private static final String DEMO_IP  = "http://162.244.228.84";  // Demo
    private static final String DEV_IP   = "http://162.244.228.78";  // Development
    private static final String LOCAL_IP = "http://192.168.1.38";    // Local
    public static final String IP = DEMO_IP;

    /** Component that build the dependencies */
    private static GraphComponent mComponent;

    @Override
    protected void attachBaseContext( Context base ) {
        super.attachBaseContext( base );
        ACRA.init( this );
    }

	@Override
    public void onCreate() {
        super.onCreate();

        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule( new ApplicationModule( this ) )
                .build();

        mComponent = DaggerGraphComponent.builder()
                .applicationComponent( appComponent )
                .apiClientModule( new ApiClientModule( IP ) )
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

    /**
     * Returns an string that represents the server of the IP
     * @return P  - production
     *         De - demo
     *         D  - development
     *         L  - local
     */
    public static String getSwitch() {
        return ( IP.equals( PROD_IP ) ) ? "P" :
               ( IP.equals( DEMO_IP ) ) ? "E" :
               ( IP.equals( DEV_IP ) ) ? "D" : "L";
    }

    public static GraphComponent getComponent() {
        return mComponent;
    }
}
