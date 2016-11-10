package co.yodo.mobile.injection.module;

import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.injection.scope.ApplicationScope;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

@Module
public class ApiClientModule {
    @Provides
    @ApplicationScope
    ProgressDialogHelper providesProgressDialogHelper() {
        return new ProgressDialogHelper();
    }

    /** URL for the requests */
    private final String mBaseUrl;

    public ApiClientModule( String baseUrl ) {
        mBaseUrl = baseUrl;
    }

    @Provides
    @ApplicationScope
    HttpLoggingInterceptor providesInterceptor() {
        HttpLoggingInterceptor.Level DEBUG;

        if( AppConfig.DEBUG ) {
            DEBUG = HttpLoggingInterceptor.Level.BODY;
        } else {
            DEBUG = HttpLoggingInterceptor.Level.NONE;
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel( DEBUG );
        return logging;
    }

    @Provides
    @ApplicationScope
    OkHttpClient providesOkHttpClient( HttpLoggingInterceptor logging ) {
        return new OkHttpClient.Builder()
                .addInterceptor( logging )
                .build();
    }

    @Provides
    @ApplicationScope
    Retrofit providesRetrofit( OkHttpClient client ) {
        return new Retrofit.Builder()
                .addConverterFactory( SimpleXmlConverterFactory.create() )
                .baseUrl( mBaseUrl )
                .client( client )
                .build();
    }
}
