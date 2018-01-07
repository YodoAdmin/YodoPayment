package co.yodo.mobile.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.AuthenticateRequest;
import co.yodo.mobile.business.service.RegistrationIntentService;
import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.model.dtos.GCMResponse;
import co.yodo.mobile.ui.PaymentActivity;
import co.yodo.mobile.ui.registration.RegistrationActivity;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.SystemUtils;

public class SplashActivity extends AppCompatActivity {
    /** The application context */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** Request for error Google Play Services */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupGUI();
        updateData();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    // Google play services installed
                    SplashActivity.newInstance(context);
                } else if (resultCode == RESULT_CANCELED) {
                    // Denied to install
                    Toast.makeText(context, R.string.error_play_services, Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
        }
    }

    /**
     * Starts a new instance of the splash screen
     * @param context The application context
     */
    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // Injection
        YodoApplication.getComponent().inject(this);
    }

    /**
     * Sets the main permissions, and values
     */
    private void updateData() {
        // Get the main booleans
        boolean hasServices = SystemUtils.isGooglePlayServicesAvailable(
                SplashActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );

        // Verify Google Play Services
        if (hasServices) {
            final String uuidToken = PreferencesHelper.getUuidToken();
            if (uuidToken != null) {
                authenticateUser(uuidToken);
            } else {
                RegistrationActivity.newInstance(context);
                finish();
            }
        }
    }

    /**
     * Validates the uuid token with the server
     */
    private void authenticateUser(final String uuidToken) {
        requestManager.invoke(
                new AuthenticateRequest(uuidToken),
                new ApiClient.RequestCallback() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        // Get response code
                        final String code = response.getCode();

                        // Do the correct action
                        switch (code) {
                            case ServerResponse.AUTHORIZED:
                                if (!PreferencesHelper.isGCMTokenSent()) {
                                    // There is no token for GCM
                                    RegistrationIntentService.newInstance(context, uuidToken);
                                } else {
                                    // Verify if the other elements have been verified
                                    finish();
                                    startNextActivity();
                                }
                                break;

                            case ServerResponse.ERROR_NOT_REGISTERED:
                                // We need to register first
                                RegistrationActivity.newInstance(context);
                                finish();
                                break;

                            default:
                                ErrorUtils.handleError(
                                        SplashActivity.this,
                                        getString(R.string.error_server),
                                        true
                                );
                                break;
                        }
                    }

                    @Override
                    public void onError(String message) {
                        ErrorUtils.handleError(
                                SplashActivity.this,
                                message,
                                true
                        );
                    }
                }
        );
    }

    /**
     * Starts the main window of the YodoPayment
     * The application, or the registration
     */
    private void startNextActivity() {
        if (PreferencesHelper.getAuthNumber() != null) {
            // The authNumber exists, so the biometric token has not been registered
            RegistrationActivity.newInstance(context);
        } else {
            // The token biometric had already been sent, we can continue
            PaymentActivity.newInstance(context);
        }
    }

    /**
     * Message received from the service that registers the gcm token
     */
    @SuppressWarnings("unused") // receives GCM receipts
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onResponseEvent(GCMResponse response) {
        EventBus.getDefault().removeStickyEvent(response);
        boolean sentToken = PreferencesHelper.isGCMTokenSent();
        if (sentToken) {
            // The gcm token has been sent
            startNextActivity();
        } else {
            // Something failed
            ErrorUtils.handleError(SplashActivity.this, response.getMessage(), true);
        }
    }
}
