package co.yodo.mobile.business.injection.component;

import com.evernote.android.job.Job;

import java.util.Map;

import javax.inject.Provider;

import co.yodo.mobile.business.injection.module.ApiClientModule;
import co.yodo.mobile.business.injection.module.CipherModule;
import co.yodo.mobile.business.injection.module.FirebaseModule;
import co.yodo.mobile.business.injection.module.JobsModule;
import co.yodo.mobile.business.injection.scope.ApplicationScope;
import co.yodo.mobile.business.service.RegistrationIntentService;
import co.yodo.mobile.business.service.YodoInstanceIDService;
import co.yodo.mobile.ui.CouponsActivity;
import co.yodo.mobile.ui.LinkedAccountsActivity;
import co.yodo.mobile.ui.PaymentActivity;
import co.yodo.mobile.ui.ReceiptsActivity;
import co.yodo.mobile.ui.registration.RegistrationActivity;
import co.yodo.mobile.ui.ResetPipActivity;
import co.yodo.mobile.ui.splash.SplashActivity;
import co.yodo.mobile.ui.adapter.CouponsAdapter;
import co.yodo.mobile.ui.registration.InputBiometricFragment;
import co.yodo.mobile.ui.registration.InputPipFragment;
import co.yodo.mobile.ui.option.SaveCouponOption;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import dagger.Component;

@ApplicationScope
@Component(
        modules = {CipherModule.class, ApiClientModule.class, JobsModule.class, FirebaseModule.class},
        dependencies = ApplicationComponent.class
)
public interface GraphComponent {
    // Injects to the Activities
    void inject(SplashActivity activity);
    void inject(RegistrationActivity activity);
    void inject(PaymentActivity activity);
    void inject(ResetPipActivity activity);
    void inject(ReceiptsActivity activity);
    void inject(CouponsActivity activity);
    void inject(LinkedAccountsActivity activity);

    // Inject to fragments
    void inject(InputPipFragment fragment);
    void inject(InputBiometricFragment fragment);

    // Injects to the Services
    void inject(RegistrationIntentService service);
    void inject(YodoInstanceIDService service);

    // Injects to the Components
    void inject(IRequestOption option);
    void inject(CouponsAdapter adapter);
    void inject(SaveCouponOption option);

    // Provides the map of executable jobs
    Map<String, Provider<Job>> provideJobs();
}
