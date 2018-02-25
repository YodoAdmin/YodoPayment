package co.yodo.mobile;

import android.util.Log;

import com.evernote.android.job.JobManager;
import com.orm.SugarApp;

import co.yodo.mobile.business.injection.component.ApplicationComponent;
import co.yodo.mobile.business.injection.component.DaggerApplicationComponent;
import co.yodo.mobile.business.injection.component.DaggerGraphComponent;
import co.yodo.mobile.business.injection.component.GraphComponent;
import co.yodo.mobile.business.injection.module.ApiClientModule;
import co.yodo.mobile.business.injection.module.ApplicationModule;
import co.yodo.mobile.business.jobs.JobHandler;
import co.yodo.mobile.business.network.Config;
import co.yodo.mobile.helper.PreferencesHelper;
import timber.log.Timber;

public class YodoApplication extends SugarApp {
    /** Component that build the dependencies */
    private static GraphComponent component;

	@Override
    public void onCreate() {
        super.onCreate();

        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        component = DaggerGraphComponent.builder()
                .applicationComponent(appComponent)
                .apiClientModule(new ApiClientModule(Config.IP, true))
                .build();

        // Init preferences
        PreferencesHelper.init(this);

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
        JobManager.create(this).addJobCreator(
                new JobHandler(component.provideJobs())
        );
    }

    /**
     * Returns an string that represents the server of the IP
     * @return P  - production
     *         De - demo
     *         D  - development
     *         L  - local
     */
    public static String getSwitch() {
        return Config.getServerIdentifier();
    }

    public static GraphComponent getComponent() {
        return component;
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
}
