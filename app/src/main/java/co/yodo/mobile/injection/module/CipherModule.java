package co.yodo.mobile.injection.module;

import android.content.Context;

import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.injection.scope.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class CipherModule {
    @Provides
    @ApplicationScope
    RSACrypt providesRSACrypt( Context context ) {
        return new RSACrypt( context );
    }
}
