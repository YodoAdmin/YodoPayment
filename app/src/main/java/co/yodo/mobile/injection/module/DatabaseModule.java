package co.yodo.mobile.injection.module;

import android.content.Context;

import co.yodo.mobile.database.provider.YodoSQLiteHelper;
import co.yodo.mobile.injection.scope.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {
    @Provides
    @ApplicationScope
    YodoSQLiteHelper providesYodoSQLiteHelper( Context context ){
        return new YodoSQLiteHelper( context );
    }
}
