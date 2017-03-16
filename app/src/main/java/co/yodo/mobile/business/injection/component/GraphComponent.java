package co.yodo.mobile.business.injection.component;

import com.evernote.android.job.Job;

import java.util.Map;

import javax.inject.Provider;

import co.yodo.mobile.business.injection.module.ApiClientModule;
import co.yodo.mobile.business.injection.module.CipherModule;
import co.yodo.mobile.business.injection.module.JobsModule;
import co.yodo.mobile.business.injection.scope.ApplicationScope;
import co.yodo.mobile.business.service.RegistrationIntentService;
import co.yodo.mobile.ui.LinkedAccountsActivity;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.ReceiptsActivity;
import co.yodo.mobile.ui.RegistrationActivity;
import co.yodo.mobile.ui.ResetPipActivity;
import co.yodo.mobile.ui.SplashActivity;
import co.yodo.mobile.ui.fragments.InputPipFragment;
import co.yodo.mobile.ui.fragments.RegistrationBiometricFragment;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import dagger.Component;

@ApplicationScope
@Component(
        modules = { CipherModule.class, ApiClientModule.class, JobsModule.class},
        dependencies = ApplicationComponent.class
)
public interface GraphComponent {
    // Injects to the Activities
    void inject( SplashActivity activity );
    void inject( RegistrationActivity activity );
    void inject( MainActivity activity );
    void inject( ResetPipActivity activity );
    void inject( ReceiptsActivity activity );
    void inject( LinkedAccountsActivity activity );

    /** Inject to fragments */
    void inject( InputPipFragment fragment );
    void inject( RegistrationBiometricFragment fragment );

    // Injects to the Services
    void inject( RegistrationIntentService service );

    // Injects to the Components
    void inject( IRequestOption option );

    // Provides the map of executable jobs
    Map<String, Provider<Job>> provideJobs();
}
