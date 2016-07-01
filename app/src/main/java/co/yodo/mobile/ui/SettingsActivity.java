package co.yodo.mobile.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.GUIUtils;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( SettingsActivity.this );
        setContentView( R.layout.activity_settings );

        setupGUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionbar = getSupportActionBar();
        if( actionbar != null )
            actionbar.setDisplayHomeAsUpEnabled( true );

        // Sets the fragment content
        getFragmentManager().beginTransaction().replace( R.id.content, new PrefsFragmentInner() ).commit();
    }

    public static class PrefsFragmentInner extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate( final Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName( AppConfig.SHARED_PREF_FILE );
            prefMgr.setSharedPreferencesMode( MODE_PRIVATE );

            addPreferencesFromResource( R.xml.config_prefs );
        }

        @Override
        public void onResume() {
            super.onResume();
            // register listener to update when a value changes
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
        }

        @Override
        public void onPause() {
            super.onPause();
            // unregister listener
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
        }

        @Override
        public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
            // If the user changed the language, then restart the app
            if( key.equals( AppConfig.SPREF_CURRENT_LANGUAGE ) ) {
                Intent i = new Intent( getActivity(), MainActivity.class );
                i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity( i );
                getActivity().finish();
            }
        }
    }
}
