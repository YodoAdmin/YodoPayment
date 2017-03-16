package co.yodo.mobile;

import android.content.Context;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.orhanobut.hawk.Hawk;
import com.orm.SugarApp;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import co.yodo.mobile.business.injection.component.ApplicationComponent;
import co.yodo.mobile.business.injection.component.DaggerApplicationComponent;
import co.yodo.mobile.business.injection.component.DaggerGraphComponent;
import co.yodo.mobile.business.injection.component.GraphComponent;
import co.yodo.mobile.business.injection.module.ApiClientModule;
import co.yodo.mobile.business.injection.module.ApplicationModule;
import co.yodo.mobile.business.jobs.JobHandler;
import timber.log.Timber;

@ReportsCrashes(formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.error_crash_toast
)
public class YodoApplication extends SugarApp {
    /** Switch server IP address */
    private static final String PROD_IP  = "http://50.56.180.133";   // Production
    private static final String DEMO_IP  = "http://162.244.228.84";  // Demo
    private static final String DEV_IP   = "http://162.244.228.78";  // Development
    private static final String LOCAL_IP = "http://192.168.1.38";    // Local
    public static final String IP = DEV_IP;

    /** Component that build the dependencies */
    private static GraphComponent component;

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

        component = DaggerGraphComponent.builder()
                .applicationComponent( appComponent )
                .apiClientModule( new ApiClientModule( IP ) )
                .build();

        // Init secure preferences
        Hawk.init(this).build();

        // Init timber
        if (BuildConfig.DEBUG) {
            // Debug
            Timber.plant( new Timber.DebugTree() {
                // Adds the line number
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });
        } else {
            // Release
            Timber.plant(new CrashReportingTree());
        }

        // Init jobs manager
        JobManager.create( this ).addJobCreator(
                new JobHandler( component.provideJobs() )
        );
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        /** The max size of a line */
        private static final int MAX_LOG_LENGTH = 4000;
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            if (message.length() < MAX_LOG_LENGTH) {
                if (priority == Log.ASSERT) {
                    Log.wtf(tag, message);
                } else {
                    Log.println(priority, tag, message);
                }
                return;
            }

            for (int i = 0, length = message.length(); i < length; i++) {
                int newLine = message.indexOf('\n', i);
                newLine = newLine != -1 ? newLine : length;
                do {
                    int end = Math.min(newLine, i + MAX_LOG_LENGTH);
                    String part = message.substring(i, end);
                    if (priority == Log.ASSERT) {
                        Log.wtf(tag, part);
                    } else {
                        Log.println(priority, tag, part);
                    }
                    i = end;
                } while (i < newLine);
            }

        }
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
        return component;
    }
}
