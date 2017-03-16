package co.yodo.mobile.business.jobs;


import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import java.util.Map;

import javax.inject.Provider;

/**
 * Created by ltalavera on 2/28/17.
 * Handles the creation of new jobs
 */
public class JobHandler implements JobCreator {
    /** Jobs to be scheduled */
    private Map<String, Provider<Job>> jobs;

    public JobHandler(Map<String, Provider<Job>> jobs) {
        this.jobs = jobs;
    }

    @Override
    public Job create(String tag) {
        Provider<Job> jobProvider = jobs.get(tag);
        return jobProvider != null ? jobProvider.get() : null;
    }
}