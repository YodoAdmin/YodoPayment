package co.yodo.mobile.ui.registration;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.component.Intents;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.RegisterRequest;
import co.yodo.mobile.business.service.RegistrationIntentService;
import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.ui.PaymentActivity;
import co.yodo.mobile.utils.SystemUtils;
import co.yodo.mobile.model.dtos.GCMResponse;
import co.yodo.mobile.ui.CameraActivity;
import co.yodo.mobile.helper.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.utils.ErrorUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hei on 05/03/17.
 * Registration of the Biometric token
 */
public class InputBiometricFragment extends Fragment {
    /** Bundle keys */
    private static final String ARG_UUID = "ARG_UUID";
    private static final String ARG_PIP = "ARG_PIP";

    /** The application context */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** Handles the progress dialogs */
    @Inject
    ProgressDialogHelper progressManager;

    /** GUI Controllers */
    @BindView( R.id.ivFace )
    ImageView ivFace;

    /** The parent activity */
    private Activity activity;

    /** Account identifiers */
    private String hardwareToken;
    private String biometricToken;
    private String pip;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;

    /** Result Activities Identifiers */
    private static final int RESULT_CAMERA_ACTIVITY = 1;

    /**
     * Creates a new fragment with parameters
     * @param pip The pip as parameter
     * @return The fragment
     */
    public static InputBiometricFragment newInstance(String hardwareToken, String pip ) {
        InputBiometricFragment fragment = new InputBiometricFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UUID, hardwareToken);
        args.putString(ARG_PIP, pip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_input_biometric, container, false );

        // Injection
        ButterKnife.bind( this, view );
        YodoApplication.getComponent().inject( this );

        setupGUI();
        updateData();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register( this );
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister( this );
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            // Permission for the camera
            case PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    showCamera();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        switch (requestCode) {
            case (RESULT_CAMERA_ACTIVITY ) :
                // Just trained the biometric recognition, let's save the token
                if (resultCode == RESULT_OK) {
                    biometricToken = data.getStringExtra(Intents.RESULT_FACE);
                    ToastMaster.makeText(context, R.string.text_face_successful, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Validates the biometric token, and does the registration process
     * to the server
     */
    public void validateBioAndRegister() {
        if( biometricToken != null ) {
            // Register the user
            progressManager.create(activity, R.string.text_register_pip);
            requestManager.invoke(
                    new RegisterRequest( hardwareToken, pip ),
                    new ApiClient.RequestCallback() {
                        @Override
                        public void onResponse( ServerResponse response ) {
                            final String code = response.getCode();

                            if( code.equals( ServerResponse.AUTHORIZED_REGISTRATION ) ) {
                                // Time to update the biometric token
                                final String authNumber = response.getAuthNumber();
                                PreferencesHelper.saveAuthNumber( authNumber );
                                updateBiometricToken( authNumber );
                            }
                            else {
                                // There was an error during the process
                                handleError( getString( R.string.error_server ) );
                            }
                        }

                        @Override
                        public void onError( String message ) {
                            handleError( message );
                        }
                    }
            );
        } else {
            // The user needs to capture a biometric token first
            ivFace.startAnimation( AnimationUtils.loadAnimation( context, R.anim.shake ) );
            Snackbar.make( ivFace, R.string.error_required_field_face, Snackbar.LENGTH_LONG ).show();
        }
    }

    /**
     * Updates the biometric token in the server
     */
    public void updateBiometricToken( String authNumber ) {
        progressManager.create( activity, R.string.text_register_bio );
        requestManager.invoke(
                new RegisterRequest( authNumber, biometricToken, RegisterRequest.RegST.BIOMETRIC ),
                new ApiClient.RequestCallback() {
                    @Override
                    public void onResponse( ServerResponse response ) {
                        final String code = response.getCode();

                        if( code.equals( ServerResponse.AUTHORIZED ) ) {
                            // Successfully registered the biometric token
                            progressManager.create( activity, R.string.text_register_gcm );
                            PreferencesHelper.saveAuthNumber( null );
                            RegistrationIntentService.newInstance( context, hardwareToken );
                        }
                        else {
                            // There was an error during the process
                            handleError( getString( R.string.error_server ) );
                        }
                    }

                    @Override
                    public void onError( String message ) {
                        handleError( message );
                    }
                }
        );
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // Gets the parent activity
        activity = getActivity();

        // Register a listener for the biometric button
        ivFace.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                boolean cameraPermission = SystemUtils.requestPermission(
                        InputBiometricFragment.this,
                        R.string.text_permission_camera,
                        Manifest.permission.CAMERA,
                        PERMISSIONS_REQUEST_CAMERA
                );

                if( cameraPermission )
                    showCamera();
            }
        } );
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        if( getArguments() != null ) {
            hardwareToken = getArguments().getString(ARG_UUID);
            pip = getArguments().getString( ARG_PIP );
        } else {
            ToastMaster.makeText( context, R.string.error_hardware, Toast.LENGTH_LONG ).show();
            activity.finish();
        }
    }

    /**
     * Starts the face recognition activity
     */
    private void showCamera() {
        Intent intent = new Intent( context, CameraActivity.class );
        startActivityForResult( intent, RESULT_CAMERA_ACTIVITY );
    }

    /**
     * Handles all the Api errors for the class
     * @param message, The message to show
     */
    private void handleError( String message ) {
        progressManager.dismiss();
        ErrorUtils.handleError(
                activity,
                message,
                false
        );
    }

    /**
     * Message received from the service that registers the gcm token
     * It can be an error
     */
    @SuppressWarnings("unused")
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onResponseEvent( GCMResponse response ) {
        EventBus.getDefault().removeStickyEvent( response );
        progressManager.dismiss();

        // Verify the registration of the GCM token
        boolean sentToken = PreferencesHelper.isGCMTokenSent();
        if( sentToken ) {
            // The gcm token has been sent
            Intent intent = new Intent( context, PaymentActivity.class );
            startActivity( intent );
            activity.finish();
        } else {
            // Something failed
            ErrorUtils.handleError( activity, response.getMessage(), true );
        }
    }
}
