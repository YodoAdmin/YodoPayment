package co.yodo.mobile.business.injection.component;

import co.yodo.mobile.business.injection.module.ApiClientModule;
import co.yodo.mobile.business.injection.module.CipherModule;
import co.yodo.mobile.business.injection.scope.ApplicationScope;
import co.yodo.mobile.business.service.RegistrationIntentService;
import co.yodo.mobile.ui.DeLinkActivity;
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
        modules = { CipherModule.class, ApiClientModule.class},
        dependencies = ApplicationComponent.class
)
public interface GraphComponent {
    // Injects to the Activities
    void inject( SplashActivity activity );
    void inject( RegistrationActivity activity );
    void inject( MainActivity activity );
    void inject( ResetPipActivity activity );
    void inject( ReceiptsActivity activity );
    void inject( DeLinkActivity activity );

    /** Inject to fragments */
    void inject( InputPipFragment fragment );
    void inject( RegistrationBiometricFragment fragment );

    // Injects to the Services
    void inject( RegistrationIntentService service );

    // Injects to the Components
    void inject( IRequestOption option );
}
