package co.yodo.mobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import javax.inject.Inject;

import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.component.Intents;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.QueryRequest;
import co.yodo.mobile.business.network.request.ResetPIPRequest;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.ui.fragments.InputPipFragment;
import co.yodo.mobile.helper.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.option.ResetPipOption;
import co.yodo.mobile.ui.option.factory.OptionsFactory;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.SystemUtils;

public class ResetPipActivity extends BaseActivity {
    /** The application context */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper progressManager;

    /** Account identifier for the reset pip and new pip */
    private String authNumber;
    private String newPip;

    /** Handle all the options of the Payment */
    private OptionsFactory optsFactory;

    /** Activity Result */
    private static final int REQUEST_FACE_ACTIVITY = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;

    /** Fragment to reset pip */
    private InputPipFragment currentFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_pip_reset );

        setupGUI( savedInstanceState );
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_FACE_ACTIVITY:
                // Successful recognition, let's change the PIP
                if( resultCode == RESULT_OK ) {
                    progressManager.create( ResetPipActivity.this );
                    requestManager.invoke(
                            new ResetPIPRequest( hardwareToken, authNumber, newPip, ResetPIPRequest.ResetST.PIP_BIO ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    progressManager.destroy();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            finish();
                                            ToastMaster.makeText( context, R.string.text_update_successful, Toast.LENGTH_LONG ).show();
                                            break;

                                        default:
                                            ErrorUtils.handleError(
                                                    ResetPipActivity.this,
                                                    getString( R.string.error_unknown ),
                                                    false
                                            );
                                            break;
                                    }
                                }

                                @Override
                                public void onError( String message ) {
                                    handleApiError( message );
                                }
                            }
                    );
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch( requestCode ) {
            // Permission for the camera
            case PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    startRecognition();
                }
                break;

            default:
                super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        }
    }

    @Override
    protected void setupGUI( Bundle savedInstanceState ) {
        super.setupGUI( savedInstanceState );
        // Injection
        YodoApplication.getComponent().inject( this );

        // Options
        optsFactory = new OptionsFactory( this );

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            currentFragment = new InputPipFragment();
            getSupportFragmentManager().beginTransaction()
                    .add( R.id.fragment_container, currentFragment )
                    .commit();
        }
    }

    /**
     * Action that validates the input of the PIP,
     * and starts the reset PIP process
     * @param v The view of the button
     */
    public void resetPip( View v ) {
        final String pip = currentFragment.validatePIP();
        if( pip != null ) {
            ( (ResetPipOption) optsFactory.getOption( OptionsFactory.Option.RESET_PIP ) )
                    .setNewPip( pip )
                    .execute();
        }
    }

    /**
     * Action in case the user forgot the PIP,
     * here the biometric token is used
     * @param v The view of the button
     */
    public void forgotPip( View v ) {
        final String pip = currentFragment.validatePIP();
        if( pip != null ) {
            newPip = pip;
            boolean cameraPermission = SystemUtils.requestPermission(
                    ResetPipActivity.this,
                    R.string.text_permission_camera,
                    Manifest.permission.CAMERA,
                    PERMISSIONS_REQUEST_CAMERA
            );

            if( cameraPermission ) {
                startRecognition();
            }
        }
    }

    /**
     * Validates the new PIP and its confirmation
     */
    private void startRecognition() {
        // Request the biometric token
        progressManager.create( this );
        requestManager.invoke(
                new QueryRequest( hardwareToken, QueryRequest.Record.BIOMETRIC ),
                new ApiClient.RequestCallback() {
                    @Override
                    public void onResponse( ServerResponse response ) {
                        progressManager.destroy();
                        final String code = response.getCode();

                        switch( code ) {
                            case ServerResponse.AUTHORIZED:
                                String biometricToken = response.getParams().getBiometricToken();

                                if( !biometricToken.equals( AppConfig.YODO_BIOMETRIC ) ) {
                                    // Start the recognition activity
                                    authNumber = response.getAuthNumber();
                                    Intent intent = new Intent( context, CameraActivity.class );
                                    intent.putExtra( Intents.BIOMETRIC_TOKEN, biometricToken );
                                    startActivityForResult( intent, REQUEST_FACE_ACTIVITY );
                                } else {
                                    // The user doesn't have a biometric token
                                    ErrorUtils.handleError(
                                            ResetPipActivity.this,
                                            getString( R.string.error_biometric ),
                                            false
                                    );
                                }
                                break;

                            default:
                                ErrorUtils.handleError(
                                        ResetPipActivity.this,
                                        getString( R.string.error_unknown ),
                                        false
                                );
                                break;
                        }
                    }

                    @Override
                    public void onError( String message ) {
                        handleApiError( message );
                    }
                }
        );
    }

    /**
     * Handles the errors from the server
     * @param message The message to display
     */
    private void handleApiError( String message ) {
        progressManager.destroy();
        ErrorUtils.handleError(
                ResetPipActivity.this,
                message,
                false
        );
    }
}
