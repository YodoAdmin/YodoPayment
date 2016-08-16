package co.yodo.mobile.injection.component;

import co.yodo.mobile.database.provider.YodoProvider;
import co.yodo.mobile.injection.module.ApiClientModule;
import co.yodo.mobile.injection.module.CipherModule;
import co.yodo.mobile.injection.module.DatabaseModule;
import co.yodo.mobile.injection.module.ValidatorModule;
import co.yodo.mobile.injection.scope.ApplicationScope;
import co.yodo.mobile.service.RegistrationIntentService;
import co.yodo.mobile.ui.DeLinkActivity;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.RegistrationActivity;
import co.yodo.mobile.ui.RegistrationBiometricActivity;
import co.yodo.mobile.ui.ResetPIPActivity;
import co.yodo.mobile.ui.SplashActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import dagger.Component;

@ApplicationScope
@Component(
        modules = { DatabaseModule.class, ValidatorModule.class, CipherModule.class, ApiClientModule.class},
        dependencies = ApplicationComponent.class
)
public interface GraphComponent {
    // Injects to the Activities
    void inject( SplashActivity activity );
    void inject( RegistrationActivity activity );
    void inject( RegistrationBiometricActivity activity );
    void inject( MainActivity activity );
    void inject( ResetPIPActivity activity );
    void inject( DeLinkActivity activity );

    // Injects to the Services
    void inject( RegistrationIntentService service );

    // Injects to the Components
    void inject( IRequestOption option );
    void inject( YodoProvider provider );
}
