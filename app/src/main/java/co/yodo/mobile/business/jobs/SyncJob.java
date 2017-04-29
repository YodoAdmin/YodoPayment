package co.yodo.mobile.business.jobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import javax.inject.Inject;

import co.yodo.mobile.business.network.ApiClient;

/**
 * Created by ltalavera on 2/28/17.
 * Synchronize the entries to the backend
 */
public class SyncJob extends Job {
    /** Job identifier */
    public static final String TAG = "sync_job_tag";

    /** The id used to find the entry */
    private static final String PARAM_ENTRY_ID = "param_entry_id";

    /** Handles the server communication */
    private final ApiClient networkManager;

    @Inject
    public SyncJob( ApiClient networkManager ) {
        this.networkManager = networkManager;
    }

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        // Get the entry to send
        PersistableBundleCompat extras = params.getExtras();

        return Result.SUCCESS;
    }

    /**
     * Schedules a job
     * @param id
     */
    public static void scheduleJob( long id ) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putLong( PARAM_ENTRY_ID, id );

        new JobRequest.Builder( TAG )
                .setExecutionWindow( 1_000L, 60_000L )
                .setRequiredNetworkType( JobRequest.NetworkType.CONNECTED )
                .setPersisted( true )
                .setRequirementsEnforced( true )
                .setExtras( extras )
                .build()
                .schedule();
    }
}
