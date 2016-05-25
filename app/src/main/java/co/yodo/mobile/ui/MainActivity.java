package co.yodo.mobile.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import co.yodo.mobile.R;
import co.yodo.mobile.component.ClearEditText;
import co.yodo.mobile.ui.component.ProgressDialogHelper;
import co.yodo.mobile.ui.component.ToastMaster;
import co.yodo.mobile.ui.component.YodoHandler;
import co.yodo.mobile.database.model.Receipt;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.database.CouponsDataSource;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.ui.component.AlertDialogHelper;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppEula;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.component.SKSCreater;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MainActivity extends AppCompatActivity implements
        YodoRequest.RESTListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifier */
    private String hardwareToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** GUI Controllers */
    private TextView mAccountNumber;
    private TextView mAccountDate;
    private TextView mAccountBalance;
    private RelativeLayout mAdvertisingLayout;
    private ImageViewTouch mAdvertisingImage;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageButton ibSubscription;

    /** Database and current merchant */
    private CouponsDataSource couponsdb;
    private ReceiptsDataSource receiptsdb;
    private String mMerchant;

    /** Provides an entry point for Google Play services. */
    private GoogleApiClient mGoogleApiClient;

    /** A {@link MessageListener} for processing messages from nearby devices. */
    private MessageListener mMessageListener;

    /** Sets the time in seconds for a published message or a subscription to live */
    private Strategy PUB_SUB_STRATEGY;
    /**
     * Tracks if we are currently resolving an error related to Nearby permissions. Used to avoid
     * duplicate Nearby permission dialogs if the user initiates both subscription and publication
     * actions without having opted into Nearby.
     */
    private boolean mResolvingNearbyPermissionError = false;

    /** SKS time to dismiss */
    private static final int TIME_TO_DISMISS_SKS = 1000 * 60; // 60 seconds

    /** Time between advertisement requests */
    private static final int DELAY_BETWEEN_REQUESTS = 1000 * 30; // 30 seconds

    /** SKS data separator */
    private static final String SKS_SEP = "**";

    /** SKS code */
    private String originalCode;

    /** PIP temporal */
    private String pipTemp;

    /** Differentiates the same query for different actions */
    private Integer queryType;
    private static final int GENERATE_SKS = 0;
    private static final int DELINK       = 1;

    /** AlertDialog for Payments */
    private AlertDialog alertDialog;
    private Dialog sksDialog;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    /** Request code to use when launching the resolution activity. */
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    // Runnable that takes care of start the scans
    private Runnable mGetAdvertisement = new Runnable() {
        @Override
        public void run() {
            mRequestManager.requestAdvertising(
                    hardwareToken,
                    mMerchant
            );
            // Wait some time for the next advertisement request
            handlerMessages.postDelayed( mGetAdvertisement, DELAY_BETWEEN_REQUESTS );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( MainActivity.this );
        setContentView( R.layout.activity_main );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // True when the activity is in foreground
        AppUtils.saveIsForeground( ac, true );
        // Register listener for requests and  broadcast receivers
        mRequestManager.setListener( this );
        // Open databases
        openDatabases();
    }

    @Override
    public void onPause() {
        super.onPause();
        // False when the activity is not in foreground
        AppUtils.saveIsForeground( ac, false );
        // Close databases
        closeDatabases();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register to event bus
        EventBus.getDefault().register( this );
        // Set listener for preferences
        AppUtils.registerSPListener( ac, this );
        // Creates the pub sub strategy for nearby
        PUB_SUB_STRATEGY = new Strategy.Builder()
                .setTtlSeconds( AppUtils.getPromotionsTime( ac ) ).build();
        // Connect to the service
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( Nearby.MESSAGES_API )
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        // Unregister from event bus
        EventBus.getDefault().unregister( this );
        // Unregister listener for preferences
        AppUtils.unregisterSPListener( ac, this );
        // Disconnect the api client if there is a connection
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            AppUtils.setSubscribing( ac, false );
            unsubscribe();
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( mDrawerToggle.onOptionsItemSelected( item ) ) {
            return true;
        }

        int id = item.getItemId();
        switch( id ) {
            case R.id.action_settings:
                Intent intent = new Intent( MainActivity.this, SettingsActivity.class );
                startActivity( intent );
                return true;

            case R.id.action_about:
                final String title   = item.getTitle().toString();
                final String message = getString( R.string.version_label ) + " " +
                                       getString( R.string.version_value ) + "/" +
                                       YodoRequest.getSwitch() + "\n\n" +
                                       getString( R.string.about_message );

                LayoutInflater inflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
                View layout = inflater.inflate( R.layout.dialog_about, new LinearLayout( this ), false );

                TextView emailView = (TextView) layout.findViewById( R.id.emailView );
                TextView messageView = (TextView) layout.findViewById( R.id.messageView );

                SpannableString email = new SpannableString( getString( R.string.about_email ) );
                email.setSpan( new UnderlineSpan(), 0, email.length(), 0 );

                emailView.setText( email );
                messageView.setText( message );

                emailView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent( Intent.ACTION_SEND );
                        String[] recipients = { getString( R.string.about_email ) };
                        intent.putExtra( Intent.EXTRA_EMAIL, recipients ) ;
                        intent.putExtra( Intent.EXTRA_SUBJECT, hardwareToken );
                        intent.setType( "text/html" );
                        startActivity( Intent.createChooser( intent, "Send Email" ) );
                    }
                });

                AlertDialogHelper.showAlertDialog(
                        ac,
                        title,
                        layout
                );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // get the context
        ac = MainActivity.this;
        handlerMessages = new YodoHandler( MainActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );

        // Globals GUI Components
        mAccountNumber     = (TextView) findViewById( R.id.accountNumberText );
        mAccountDate       = (TextView) findViewById( R.id.accountDateText );
        mAccountBalance    = (TextView) findViewById( R.id.accountBalanceText );
        mAdvertisingLayout = (RelativeLayout) findViewById( R.id.advertisingLayout );
        mAdvertisingImage  = (ImageViewTouch) findViewById( R.id.advertisingImage );
        mDrawerLayout      = (DrawerLayout) findViewById(R.id.drawerLayout);
        ibSubscription     = (ImageButton) findViewById( R.id.ibSubscription );

        // Images fit parent
        mAdvertisingImage.setDisplayType( ImageViewTouchBase.DisplayType.FIT_TO_SCREEN );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed( drawerView );
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened( drawerView );
            }
        };

        mDrawerLayout.addDrawerListener( mDrawerToggle );

        // Set up the listener for the Nearby messages
        initializeMessageListener();

        couponsdb  = new CouponsDataSource( ac );
        receiptsdb = ReceiptsDataSource.getInstance( ac );
        //receiptsdb = new ReceiptsDataSource( ac );

        mAdvertisingImage.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean writePermission = AppUtils.requestPermission(
                        MainActivity.this,
                        R.string.message_permission_write_external_storage,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                );

                final Drawable drawable = mAdvertisingImage.getDrawable();

                if( !writePermission || drawable == null )
                    return false;

                DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Bitmap bitmap = AppUtils.drawableToBitmap( drawable );
                        File directory = new File( Environment.getExternalStorageDirectory(), AppConfig.COUPONS_FOLDER );
                        boolean success = true;

                        if( !directory.exists() )
                            success = directory.mkdir();

                        if( !success ) {
                            Toast.makeText( ac, R.string.image_saved_failed, Toast.LENGTH_SHORT ).show();
                            return;
                        }

                        int files = directory.listFiles().length;
                        File image = new File( directory, "ad" + (files + 1) + ".png" );

                        FileOutputStream outStream;
                        try {
                            outStream = new FileOutputStream( image );
                            bitmap.compress( Bitmap.CompressFormat.PNG, 90, outStream );

                            outStream.flush();
                            outStream.close();
                            couponsdb.createCoupon( image.getPath(), mMerchant );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

                AlertDialogHelper.showAlertDialog(
                        ac,
                        R.string.save_image,
                        onClick
                );

                return true;
            }
        });

        if( AppUtils.isFirstLogin( ac ) ) {
            mDrawerLayout.openDrawer( GravityCompat.START );
            AppUtils.saveFirstLogin( ac, false );
        }

        // Show the terms, if the app is updated
        AppEula.show( this );
        // Upon orientation change, ensure that the state of the UI is maintained.
        updateUI();
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        // Gets the hardware token - account identifier
        hardwareToken = AppUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
        // Set the account number and current date
        mAccountNumber.setText( hardwareToken );
        mAccountDate.setText( AppUtils.getCurrentDate() );
    }

    /**
     * Updates the UI when the state of a subscription or
     * publication action changes.
     */
    private void updateUI() {
        Boolean subscriptionTask = AppUtils.isSubscribing( ac );
        ibSubscription.setImageResource(
                subscriptionTask ? R.drawable.ic_cancel : R.drawable.ic_nearby
        );
        if( !subscriptionTask )
            removeAdvertisement();
    }

    /**
     * Stops the advertisement requests
     */
    private void removeAdvertisement() {
        mMerchant = null;
        handlerMessages.removeCallbacks( mGetAdvertisement );
        mAdvertisingImage.setImageDrawable( null );
    }

    /**
     * Initializes the listener for the nearby API
     */
    private void initializeMessageListener() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound( final Message message ) {
                mMerchant = new String( message.getContent() );
                // Called when a message is detectable nearby.
                AppUtils.Logger( TAG, "Found: " + mMerchant );
                handlerMessages.post( mGetAdvertisement );
            }

            @Override
            public void onLost( final Message message ) {
                mMerchant = new String( message.getContent() );
                // Called when a message is no longer detectable nearby.
                AppUtils.Logger( TAG, "Lost: " + mMerchant );
                removeAdvertisement();
            }
        };
    }

    /**
     * Subscribes to messages from nearby devices. If not successful, attempts to resolve any error
     * related to Nearby permissions by displaying an opt-in dialog. Registers a callback which
     * updates state when the subscription expires.
     */
    private void subscribe() {
        AppUtils.Logger( TAG, "trying to subscribe" );
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if( !mGoogleApiClient.isConnected() ) {
            if( !mGoogleApiClient.isConnecting() ) {
                mGoogleApiClient.connect();
            }
        } else {
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy( PUB_SUB_STRATEGY )
                    .setCallback( new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            AppUtils.Logger( TAG, "no longer subscribing" );
                            AppUtils.setSubscribing( ac, false );
                        }
                    }).build();

            Nearby.Messages.subscribe( mGoogleApiClient, mMessageListener, options )
                    .setResultCallback( new ResultCallback<Status>() {
                        @Override
                        public void onResult( @NonNull Status status ) {
                            if( status.isSuccess() ) {
                                AppUtils.Logger( TAG, "subscribed successfully" );
                                AppUtils.setSubscribing( ac, true );
                            } else {
                                AppUtils.Logger( TAG, "could not subscribe" );
                                AppUtils.setSubscribing( ac, false );
                                handleUnsuccessfulNearbyResult( status );
                            }
                        }
                    });
        }
    }

    /**
     * Ends the subscription to messages from nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by
     * displaying an opt-in dialog.
     */
    private void unsubscribe() {
        AppUtils.Logger( TAG, "trying to unsubscribe" );
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if( !mGoogleApiClient.isConnected()  ) {
            if( !mGoogleApiClient.isConnecting() ) {
                mGoogleApiClient.connect();
            }
        } else {
            Nearby.Messages.unsubscribe( mGoogleApiClient, mMessageListener )
                    .setResultCallback( new ResultCallback<Status>() {
                        @Override
                        public void onResult( @NonNull Status status ) {
                            if( status.isSuccess() ) {
                                AppUtils.Logger( TAG, "unsubscribed successfully" );
                                AppUtils.setSubscribing( ac, false );
                            } else {
                                AppUtils.Logger( TAG, "could not unsubscribe" );
                                AppUtils.setSubscribing( ac, true );
                                handleUnsuccessfulNearbyResult( status );
                            }
                        }
                    });
        }
    }

    /**
     * Handles errors generated when performing a subscription or publication action. Uses
     * {@link Status#startResolutionForResult} to display an opt-in dialog to handle the case
     * where a device is not opted into using Nearby.
     */
    private void handleUnsuccessfulNearbyResult( Status status ) {
        AppUtils.Logger( TAG, "processing error, status = " + status );
        if( status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN ) {
            if( !mResolvingNearbyPermissionError ) {
                try {
                    mResolvingNearbyPermissionError = true;
                    status.startResolutionForResult(
                            this,
                            REQUEST_RESOLVE_ERROR
                    );
                } catch( IntentSender.SendIntentException e ) {
                    e.printStackTrace();
                }
            }
        } else {
            if( status.getStatusCode() == ConnectionResult.NETWORK_ERROR ) {
                Toast.makeText( ac, R.string.message_error_no_connectivity, Toast.LENGTH_LONG ).show();
            } else {
                // To keep things simple, pop a toast for all other error messages.
                Toast.makeText( ac, "Unsuccessful: " + status.getStatusMessage(), Toast.LENGTH_LONG ).show();
            }
        }
    }

    /**
     * Invokes a pending task based on the subscription state.
     */
    private void executePendingSubscriptionTask() {
        if( AppUtils.isSubscribing( ac ) ) {
            subscribe();
        } else {
            unsubscribe();
        }
    }

    /**
     * Tries to subscribe to close Rocket (POS) devices
     * for advertisement
     * @param v The view, used to change the icon
     */
    public void getPromotionsClick( View v ) {
        if( !AppUtils.isSubscribing( ac ) ) {
            AppUtils.setSubscribing( ac, true );
        } else {
            AppUtils.setSubscribing( ac, false );
        }
        executePendingSubscriptionTask();
    }

    /**
     * Asks for the PIP to realize a payment
     * @param v The view, not used
     */
    public void paymentClick( View v ) {
        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( MainActivity.this );

                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                    ToastMaster.makeText( MainActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                } else {
                    mAdvertisingLayout.setVisibility( View.GONE );
                    originalCode = pip + SKS_SEP + hardwareToken + SKS_SEP;
                    pipTemp   = pip;
                    queryType = GENERATE_SKS;

                    ProgressDialogHelper.getInstance().createProgressDialog( ac );

                    YodoRequest.getInstance( ac ).requestClientAuth(
                            hardwareToken, pip
                    );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                null, null,
                inputBox,
                onClick
        );
    }

    /**
     * List of saved coupons
     * @param v, not used
     */
    public void couponsClick(View v) {
        Intent intent = new Intent( MainActivity.this, CouponsActivity.class );
        startActivity( intent );
    }

    /**
     * Future feature
     * @param v, not used
     */
    public void networkClick(View v) {
        Snackbar.make( mDrawerLayout, R.string.no_available, Snackbar.LENGTH_SHORT ).show();
    }

    /**
     * Changes the current PIP
     * @param v, not used
     */
    public void resetPipClick(View v) {
        mDrawerLayout.closeDrawers();

        Intent intent = new Intent( MainActivity.this, PipResetActivity.class );
        startActivity( intent );
    }

    /**
     * Get the saved receipts
     * @param v, not used
     */
    public void savedReceiptsClick(View v) {
        mDrawerLayout.closeDrawers();

        Intent intent = new Intent( MainActivity.this, ReceiptsActivity.class );
        startActivity( intent );
    }

    /**
     * Link the account with another
     * @param v, not used
     */
    public void linkAccountsClick(View v) {
        mDrawerLayout.closeDrawers();

        final EditText inputBox = new ClearEditText( ac );
        String[] options = getResources().getStringArray( R.array.link_options_array );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int item) {
                switch( item ) {
                    case 0:
                        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item1) {
                                String pip = inputBox.getText().toString();
                                AppUtils.hideSoftKeyboard( MainActivity.this );

                                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                                    ToastMaster.makeText( MainActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                                } else {
                                    ProgressDialogHelper.getInstance().createProgressDialog( ac );
                                    mRequestManager.requestLinkingCode(
                                            hardwareToken,
                                            pip
                                    );
                                }
                            }
                        };

                        AlertDialogHelper.showAlertDialog(
                                ac,
                                getString( R.string.input_pip ),
                                null, null,
                                inputBox,
                                onClick
                        );
                        break;

                    case 1:
                        String title = getString( R.string.input_linking_code );

                        onClick = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String linkingCode = inputBox.getText().toString();

                                ProgressDialogHelper.getInstance().createProgressDialog( ac );
                                mRequestManager.requestLinkAccount(
                                        hardwareToken,
                                        linkingCode
                                );
                            }
                        };

                        AlertDialogHelper.showAlertDialog(
                                ac,
                                title,
                                null, getString( R.string.show_linking_code ),
                                inputBox,
                                onClick
                        );

                        break;

                    case 2:
                        title = getString( R.string.input_pip );

                        onClick = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item1) {
                                String pip = inputBox.getText().toString();
                                AppUtils.hideSoftKeyboard( MainActivity.this );

                                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                                    ToastMaster.makeText( MainActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                                } else {
                                    pipTemp   = pip;
                                    queryType = DELINK;

                                    ProgressDialogHelper.getInstance().createProgressDialog( ac );
                                    mRequestManager.requestLinkedAccounts(
                                            hardwareToken,
                                            pip
                                    );
                                }
                            }
                        };

                        AlertDialogHelper.showAlertDialog(
                                ac,
                                title,
                                null, null,
                                inputBox,
                                onClick
                        );

                        break;
                }
            }
        };

        AlertDialogHelper.showAlertDialog( ac, getString( R.string.linking_menu ), options, onClick );
    }

    /**
     * Get the user balance
     * @param v, The button view, not used
     */
    public void balanceClick( View v ) {
        mDrawerLayout.closeDrawers();

        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( MainActivity.this );

                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                    ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                } else {
                    ProgressDialogHelper.getInstance().createProgressDialog( ac );
                    mRequestManager.requestBalance(
                            hardwareToken,
                            pip
                    );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                null, null,
                inputBox,
                onClick
        );
    }

    /**
     * Closes the client account
     * @param v, not used
     */
    public void closeAccountClick(View v) {
        mDrawerLayout.closeDrawers();

        final String title      = getString( R.string.input_pip );
        final String message    = getString( R.string.close_message );
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( MainActivity.this );

                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                    ToastMaster.makeText( MainActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                } else {
                    ProgressDialogHelper.getInstance().createProgressDialog( ac );
                    mRequestManager.requestCloseAccount(
                            hardwareToken,
                            pip
                    );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                message, null,
                inputBox,
                onClick
        );
    }

    public void alternatePaymentClick( View v ) {
        final ImageView accountImage = (ImageView) v;
        Integer account_type = Integer.parseInt( accountImage.getContentDescription().toString() );

        if( account_type == 0 ) {
            account_type = null;
        }

        showSKSDialog( originalCode, account_type );
        originalCode = null;

        alertDialog.dismiss();
        alertDialog = null;
    }

    /**
     * Open/Close the databases.
     */
    private void openDatabases() {
        if( couponsdb != null )
            couponsdb.open();

        if( receiptsdb != null )
            receiptsdb.open();
    }

    private void closeDatabases() {
        if( couponsdb != null )
            couponsdb.close();

        if( receiptsdb != null )
            receiptsdb.close();
    }

    /**
     * Method to show the dialog containing the SKS code
     */
    private void showSKSDialog( final String code, final Integer account_type ) {
        try {
            final Bitmap qrCode = SKSCreater.createSKS( code, this, SKSCreater.SKS_CODE, account_type );
            //final Dialog sksDialog = new Dialog( this );
            sksDialog = new Dialog( this );

            sksDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
            sksDialog.setContentView( R.layout.dialog_sks );
            sksDialog.setCancelable( false );

            // brightness
            final WindowManager.LayoutParams lp = getWindow().getAttributes();
            final float brightnessNow = lp.screenBrightness;

            sksDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    lp.screenBrightness = 100 / 100.0f;
                    getWindow().setAttributes( lp );
                }
            });

            sksDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if( keyCode == KeyEvent.KEYCODE_BACK )
                        dialog.dismiss();
                    return true;
                }
            });

            sksDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    lp.screenBrightness = brightnessNow;
                    getWindow().setAttributes( lp );
                    mAdvertisingLayout.setVisibility( View.VISIBLE );
                }
            });

            ImageView image = (ImageView) sksDialog.findViewById( R.id.sks );
            image.setImageBitmap( qrCode );

            sksDialog.show();

            final Handler t = new Handler();
            t.postDelayed( new Runnable() {
                @Override
                public void run() {
                    if( sksDialog.isShowing() )
                        sksDialog.dismiss();
                }
            }, TIME_TO_DISMISS_SKS );
        } catch( UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
    }

    /**
     * BUild the receipt from the params (ServerResponse) obtained through GCM
     * @param params All the parameters to be set in the receipt
     */
    private void receiptDialog( final HashMap<String, String> params ) {
        final Dialog receipt = new Dialog( MainActivity.this );
        receipt.requestWindowFeature( Window.FEATURE_NO_TITLE );

        LayoutInflater inflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_receipt, new LinearLayout( this ), false );

        TextView descriptionText    = (TextView)  layout.findViewById( R.id.descriptionText );
        TextView authNumberText     = (TextView)  layout.findViewById( R.id.authNumberText );
        TextView createdText        = (TextView)  layout.findViewById( R.id.createdText );
        TextView currencyText       = (TextView)  layout.findViewById( R.id.currencyText );
        TextView totalAmountText    = (TextView)  layout.findViewById( R.id.paidText );
        TextView tenderAmountText   = (TextView)  layout.findViewById( R.id.cashTenderText );
        TextView cashBackAmountText = (TextView)  layout.findViewById( R.id.cashBackText );
        TextView tvDonorText        = (TextView)  layout.findViewById( R.id.tvDonorText );
        TextView tvReceiverText     = (TextView)  layout.findViewById( R.id.tvReceiverText );
        ImageView deleteButton      = (ImageView) layout.findViewById( R.id.deleteButton );
        ImageView saveButton        = (ImageView) layout.findViewById( R.id.saveButton );
        LinearLayout donorLayout    = (LinearLayout) layout.findViewById( R.id.donorAccountLayout );

        final String authNumber     = params.get( ServerResponse.AUTHNUMBER );
        final String description    = params.get( ServerResponse.DESCRIPTION );
        final String tCurrency      = params.get( ServerResponse.TCURRENCY ); // Merchant currency
        final String exchRate       = params.get( ServerResponse.EXCH_RATE );
        final String dCurrency      = params.get( ServerResponse.DCURRENCY ); // Tender currency
        final String totalAmount    = AppUtils.truncateDecimal( params.get( ServerResponse.AMOUNT ) );
        final String tenderAmount   = AppUtils.truncateDecimal( params.get( ServerResponse.TAMOUNT ) );
        final String cashBackAmount = AppUtils.truncateDecimal( params.get( ServerResponse.CASHBACK ) );
        final String balance        = AppUtils.truncateDecimal( params.get( ServerResponse.BALANCE ) );
        final String currency       = params.get( ServerResponse.CURRENCY ); // Account currency
        final String donor          = params.get( ServerResponse.DONOR );
        final String receiver       = params.get( ServerResponse.RECEIVER );
        final String created        = params.get( ServerResponse.CREATED);

        descriptionText.setText( description );
        authNumberText.setText( authNumber );
        createdText.setText( AppUtils.UTCtoCurrent( ac, created ) );
        currencyText.setText( dCurrency );
        totalAmountText.setText( String.format( "%s %s", totalAmount, tCurrency ) );
        tenderAmountText.setText( tenderAmount );
        cashBackAmountText.setText( cashBackAmount );

        mAccountBalance.setText( String.format( "%s %s", balance, currency ) );

        if( donor != null ) {
            donorLayout.setVisibility( View.VISIBLE );
            tvDonorText.setText( donor );
        }
        tvReceiverText.setText( receiver );

        deleteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                // Dismiss SKS and receipt
                if( donor == null )
                    sksDialog.dismiss();
                receipt.dismiss();
                // Show a notification which can reverse the delete
                Snackbar.make( mDrawerLayout, R.string.message_deleted_receipt, Snackbar.LENGTH_LONG )
                        .setAction( R.string.message_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                v.setEnabled( false );
                                receiptDialog( params );
                            }
                        } ).show();
            }
        } );

        saveButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Receipt item = receiptsdb.createReceipt(
                        authNumber, description, tCurrency, exchRate,
                        dCurrency, totalAmount, tenderAmount, cashBackAmount,
                        balance, currency, donor, receiver, created
                );
                // Dismiss SKS and receipt
                if( donor == null && sksDialog != null )
                    sksDialog.dismiss();
                receipt.dismiss();
                // Show a notification which can reverse the save
                Snackbar.make( mDrawerLayout, R.string.saved_receipt, Snackbar.LENGTH_LONG )
                        .setAction( R.string.message_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                v.setEnabled( false );
                                receiptsdb.deleteReceipt( item );
                                receiptDialog( params );
                            }
                        } ).show();
            }
        });

        receipt.setCancelable( false );
        receipt.setContentView( layout );
        receipt.show();
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        if( type != YodoRequest.RequestType.QUERY_ADV_REQUEST )
            ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case AUTH_PIP_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    originalCode += response.getRTime();

                    ProgressDialogHelper.getInstance().createProgressDialog( ac );

                    mRequestManager.requestLinkedAccounts(
                            hardwareToken,
                            pipTemp
                    );
                } else {
                    mAdvertisingLayout.setVisibility( View.VISIBLE );
                    message = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;

            case QUERY_BAL_REQUEST:
                code = response.getCode();

                switch( code ) {
                    case ServerResponse.AUTHORIZED_BALANCE:
                        final String tvBalance =
                                AppUtils.truncateDecimal( response.getParam( ServerResponse.BALANCE ) ) + " " +
                                response.getParam( ServerResponse.CURRENCY );
                        // Trim the balance
                        mAccountBalance.setText( tvBalance );
                        break;

                    case ServerResponse.ERROR_NO_BALANCE:
                        mAccountBalance.setText( "" );
                        Snackbar.make( mDrawerLayout, R.string.message_error_no_balance, Snackbar.LENGTH_SHORT ).show();
                        break;

                    default:
                        mAccountBalance.setText( "" );
                        message = response.getMessage();
                        AppUtils.sendMessage( handlerMessages, code, message );
                        break;
                }
                break;

            case QUERY_ADV_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String url = response.getParam( ServerResponse.ADVERTISING ).replaceAll( " ", "%20" );
                    if( !url.isEmpty() ) {
                        mRequestManager.getImageLoader().get( url, new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse( ImageLoader.ImageContainer response, boolean isImmediate ) {
                                    if( response.getBitmap() != null ) {
                                        // load image into ImageView
                                        mAdvertisingImage.setImageBitmap( response.getBitmap() );
                                    }
                                }

                                @Override
                                public void onErrorResponse( VolleyError error ) {
                                    AppUtils.Logger( TAG, "Image Load Error: " + error.getMessage() );
                                }
                            }
                        );
                    }
                }
                break;

            case QUERY_LNK_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String linking_code = response.getParam( ServerResponse.LINKING_CODE );

                    Dialog dialog = new Dialog( ac );
                    dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
                    dialog.setContentView( R.layout.dialog_linking_code );

                    final TextView codeText   = (TextView) dialog.findViewById(R.id.codeText);
                    ImageView codeImage = (ImageView) dialog.findViewById(R.id.copyCodeImage);
                    codeText.setText( linking_code );

                    codeImage.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppUtils.copyCode( ac, codeText.getText().toString() );
                            ToastMaster.makeText( ac, R.string.copied_text, Toast.LENGTH_SHORT ).show();
                        }
                    });

                    dialog.show();
                } else {
                    message = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }
                break;

            case QUERY_LNK_ACC_REQUEST:
                code = response.getCode();
                String from = response.getParam( ServerResponse.FROM );

                // This needs an improvement (handling correctly the error codes)
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    if( queryType == GENERATE_SKS ) {
                        if( !from.isEmpty() ) {
                            LayoutInflater inflater = ( LayoutInflater ) getSystemService( LAYOUT_INFLATER_SERVICE );
                            View layout = inflater.inflate( R.layout.dialog_payment, new LinearLayout( ac ), false );
                            alertDialog = AlertDialogHelper.showAlertDialog( ac, layout, getString( R.string.linking_menu ) );
                        } else {
                            showSKSDialog( originalCode, null );
                            originalCode = null;
                        }
                    } else if( queryType == DELINK ) {
                        String to = response.getParam( ServerResponse.TO );

                        Intent i = new Intent( ac, DeLinkActivity.class );
                        i.putExtra( Intents.LINKED_ACC_TO, to );
                        i.putExtra( Intents.LINKED_ACC_FROM, from );
                        i.putExtra( Intents.LINKED_PIP, pipTemp );
                        startActivity( i );
                    }
                } else if( queryType == DELINK ) {
                    //message = response.getMessage();
                    message = getString( R.string.error_message_no_links );
                    AppUtils.sendMessage( handlerMessages, code, message );
                } else {
                    message = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                pipTemp   = null;
                queryType = null;
                break;

            case LINK_ACC_REQUEST:
                code = response.getCode();
                message = response.getMessage();
                AppUtils.sendMessage( handlerMessages, code, message );
                break;

            case CLOSE_ACC_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.clearPrefConfig( ac );

                    couponsdb.delete();

                    DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    };

                    AlertDialogHelper.showAlertDialog(
                            ac,
                            getString( R.string.farewell_message_tittle ),
                            getString( R.string.farewell_message ),
                            onClick
                    );
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }
                break;
        }
    }

    @SuppressWarnings("unused") // receives GCM receipts
    @Subscribe( threadMode = ThreadMode.MAIN )
    public void onResponseEvent( ServerResponse response ) {
        receiptDialog( response.getParams() );
    }

    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        } );
    }

    @Override
    public void onConnected( @Nullable Bundle bundle ) {
        AppUtils.Logger( TAG, "GoogleApiClient connected" );
        executePendingSubscriptionTask();
    }

    @Override
    public void onConnectionSuspended( int cause ) {
        AppUtils.Logger( TAG, "GoogleApiClient connection suspended: " +  cause );
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult connectionResult ) {
        AppUtils.Logger( TAG, "connection to GoogleApiClient failed" );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        mResolvingNearbyPermissionError = false;
        if( requestCode == REQUEST_RESOLVE_ERROR ) {
            // User was presented with the Nearby opt-in dialog and pressed "Allow".
            if( resultCode == RESULT_OK ) {
                // We track the pending subscription and publication tasks. Once
                // a user gives consent to use Nearby, we execute those tasks.
                executePendingSubscriptionTask();
            } else if( resultCode == RESULT_CANCELED ) {
                // User was presented with the Nearby opt-in dialog and pressed "Deny". We cannot
                // proceed with any pending subscription and publication tasks. Reset state.
                AppUtils.setSubscribing( ac, false );
            } else {
                Toast.makeText(
                        this,
                        getString( R.string.message_error_code ) + resultCode,
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
