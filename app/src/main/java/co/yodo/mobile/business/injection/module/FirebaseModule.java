package co.yodo.mobile.business.injection.module;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;

import co.yodo.mobile.business.injection.scope.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class FirebaseModule {
    @Provides
    @ApplicationScope
    FirebaseAuth providesFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @ApplicationScope
    PhoneAuthProvider providesPhoneProvider(FirebaseAuth auth) {
        return PhoneAuthProvider.getInstance(auth);
    }
}
