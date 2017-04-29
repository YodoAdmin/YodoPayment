package co.yodo.mobile.business.injection.module;

import com.evernote.android.job.Job;

import co.yodo.mobile.business.jobs.SyncJob;
import co.yodo.mobile.business.network.ApiClient;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public class JobsModule {
    @Provides
    @IntoMap
    @StringKey(SyncJob.TAG)
    Job providesSyncJob( ApiClient networkManager) {
        return new SyncJob( networkManager );
    }
}
