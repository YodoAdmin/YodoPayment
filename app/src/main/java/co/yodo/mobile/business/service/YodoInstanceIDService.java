package co.yodo.mobile.business.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.RegisterRequest;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.model.dtos.GCMResponse;
import timber.log.Timber;

/**
 * Created by yodop on 2017-07-27.
 * Handle the creation, rotation, and updating of registration tokens
 */
public class YodoInstanceIDService extends FirebaseInstanceIdService {
    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    @Override
    public void onCreate() {
        YodoApplication.getComponent().inject( this );
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.i("Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Send the GCM token to the server
        /*requestManager.invoke(
                new RegisterRequest( hardwareToken, token, RegisterRequest.RegST.GCM ),
                new ApiClient.RequestCallback() {
                    @Override
                    public void onResponse( ServerResponse response ) {
                        final String code = response.getCode();
                        if( code.equals( ServerResponse.AUTHORIZED ) ) {
                            // If the token was successfully sent to the server
                            PrefUtils.saveGCMTokenSent( true );

                            // Notify UI that registration has completed, so the progress indicator can be hidden.
                            GCMResponse notify = new GCMResponse( response.getMessage() );
                            EventBus.getDefault().postSticky( notify );
                        } else {
                            // If there was an error in the server
                            handleApiError( getString( R.string.error_server ) );
                        }
                    }

                    @Override
                    public void onError( String message ) {
                        handleApiError( message );
                    }
                }
        );*/
    }

    /**
     * Handles any error from the server/google/device at the GCM registration time
     * @param message The error message to be displayed
     */
    private void handleApiError( String message ) {
        PrefUtils.saveGCMTokenSent( false );

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        GCMResponse notify = new GCMResponse( message );
        EventBus.getDefault().postSticky( notify );
    }
}
