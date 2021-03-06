package co.yodo.mobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatButton;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.helper.EulaHelper;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.fragments.BiometricFragment;
import co.yodo.mobile.ui.fragments.InputPipFragment;

public class RegistrationActivity extends BaseActivity {
    /** The application context */
    @Inject
    Context context;

    /** GUI Controllers */
    @BindView( R.id.acbRegister )
    AppCompatButton registerButton;

    /** Fragment tags */
    private static final String TAG_REG_PIP = "TAG_REG_PIP";
    private static final String TAG_REG_BIO = "TAG_REG_BIO";

    /** Fragment handler */
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_registration );

        setupGUI( savedInstanceState );
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void setupGUI( final Bundle savedInstanceState ) {
        super.setupGUI( savedInstanceState );
        // Injection
        YodoApplication.getComponent().inject( this );

        // Setup the fragment manager
        fragmentManager = getSupportFragmentManager();

        // Show the terms to the user
        EulaHelper.show( this, new EulaHelper.EulaCallback() {
            @Override
            public void onEulaAgreedTo() {
                registerButton.setVisibility( View.VISIBLE );

                // Check that the activity is using the layout version with
                // the fragment_container FrameLayout
                if (findViewById(R.id.fragment_container) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }

                    // Create a new Fragment to be placed in the activity layout
                    Fragment fragment;
                    if( PrefUtils.getAuthNumber() == null ) {
                        fragment = new InputPipFragment();
                    } else {
                        fragment = new BiometricFragment();
                    }
                    fragmentManager.beginTransaction()
                            .add( R.id.fragment_container, fragment, TAG_REG_PIP )
                            .commit();
                }
            }
        } );
    }

    /**
     * The next button for the registration, handles several actions depending
     * in the current fragment
     * @param view, The view, not used
     */
    public void next( View view ) {
        Fragment currentFragment = fragmentManager.findFragmentById( R.id.fragment_container );
        if( currentFragment.getTag().equals( TAG_REG_PIP ) ) {
            final String pip = ( (InputPipFragment ) currentFragment ).validatePIP();
            if( pip != null ) {
                BiometricFragment bioFragment = BiometricFragment.newInstance(
                        hardwareToken,
                        pip
                );
                fragmentManager.beginTransaction()
                        .replace( R.id.fragment_container, bioFragment, TAG_REG_BIO )
                        .addToBackStack( null )
                        .commit();
            }
        } else {
            BiometricFragment bioFragment = ( ( BiometricFragment ) currentFragment );
            final String authNumber = PrefUtils.getAuthNumber();
            if( authNumber == null ) {
                bioFragment.validateBioAndRegister();
            } else {
                bioFragment.updateBiometricToken( authNumber );
            }
        }
    }
}
