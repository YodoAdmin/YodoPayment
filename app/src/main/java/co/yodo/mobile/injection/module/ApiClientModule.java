package co.yodo.mobile.injection.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.injection.scope.ApplicationScope;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.OkHttp3Stack;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class ApiClientModule {
    @Provides
    @ApplicationScope
    public ProgressDialogHelper providesProgressDialogHelper() {
        return new ProgressDialogHelper();
    }

    @Provides
    @ApplicationScope
    OkHttp3Stack providesOkHttpStack() {
        return new OkHttp3Stack( new OkHttpClient() );
    }

    @Provides
    @ApplicationScope
    RequestQueue providesRequestQueue( Context context, OkHttp3Stack stack ) {
        return Volley.newRequestQueue( context, stack );
    }

    @Provides
    @ApplicationScope
    ImageLoader providesImageLoader( RequestQueue queue ) {
        return new ImageLoader( queue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<>( 10 );

                    @Override
                    public Bitmap getBitmap( String url) {
                        return cache.get( url );
                    }

                    @Override
                    public void putBitmap( String url, Bitmap bitmap ) {
                        cache.put( url, bitmap );
                    }
                }
        );
    }

    @Provides
    @ApplicationScope
    public ApiClient providesApiClient( RequestQueue queue, ImageLoader imageLoader, RSACrypt encrypter ) {
        return new ApiClient( queue, imageLoader, encrypter );
    }
}
