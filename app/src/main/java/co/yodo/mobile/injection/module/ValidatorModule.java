package co.yodo.mobile.injection.module;

import android.content.Context;

import co.yodo.mobile.injection.scope.ApplicationScope;
import co.yodo.mobile.ui.validator.PIPValidator;
import dagger.Module;
import dagger.Provides;

@Module
public class ValidatorModule {
    @Provides
    @ApplicationScope
    PIPValidator providesPIPValidator( Context context ){
        return new PIPValidator( context );
    }
}
