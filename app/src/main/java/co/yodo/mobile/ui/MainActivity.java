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
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.component.SKSCreater;
import co.yodo.mobile.database.CouponsDataSource;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.database.model.Receipt;
import co.yodo.mobile.helper.EulaUtils;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.AuthenticateRequest;
import co.yodo.mobile.network.request.CloseRequest;
import co.yodo.mobile.network.request.LinkRequest;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.extension.AboutOption;
import co.yodo.mobile.ui.extension.BalanceOption;
import co.yodo.mobile.ui.extension.CloseAccountOption;
import co.yodo.mobile.ui.extension.DeLinkAccountOption;
import co.yodo.mobile.ui.extension.LinkAccountOption;
import co.yodo.mobile.ui.extension.LinkCodeOption;
import co.yodo.mobile.ui.extension.PaymentOption;
import co.yodo.mobile.ui.extension.SaveCouponOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
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
    private YodoHandler handlerMessages;

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

    /** Options from the main window */
    private PaymentOption mPaymentOption;
    private SaveCouponOption mSaveCouponOption;
    private AboutOption mAboutOption;

    /** Options from the navigation window */
    private BalanceOption mBalanceOption;
    private LinkCodeOption mLinkCodeOption;
    private LinkAccountOption mLinkAccountOption;
    private DeLinkAccountOption mDeLinkAccountOption;
    private CloseAccountOption mCloseAccountOption;

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
    private String tempPIP;

    /** AlertDialog for Payments */
    private AlertDialog alertDialog;
    private Dialog sksDialog;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    /** Request code to use when launching the resolution activity. */
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    /** Response codes for the server requests */
    private static final int AUTH_REQ              = 0x00;
    private static final int QUERY_BAL_REQ         = 0x01;
    private static final int QUERY_ADV_REQ         = 0x02;
    private static final int QUERY_LNK_REQ         = 0x03;
    private static final int QUERY_LNK_ACC_SKS_REQ = 0x04;
    private static final int QUERY_LNK_ACC_DEL_REQ = 0x05;
    private static final int CLOSE_REQ             = 0x06;
    private static final int LINK_REQ              = 0x07;

    // Runnable that takes care of start the scans
    private Runnable mGetAdvertisement = new Runnable() {
        @Override
        public void run() {
            mRequestManager.invoke( new QueryRequest(
                    QUERY_ADV_REQ,
                    hardwareToken,
                    mMerchant,
                    QueryRequest.Record.ADVERTISING
            ) );
            // Wait some time for the next advertisement request
            handlerMessages.postDelayed( mGetAdvertisement, DELAY_BETWEEN_REQUESTS );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( MainActivity.this );
        setContentView( R.layout.activity_main );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // True when the activity is in foreground
        PrefUtils.saveIsForeground( ac, true );
        // Register listener for requests and  broadcast receivers
        mRequestManager.setListener( this );
        // Open databases
        openDatabases();
    }

    @Override
    public void onPause() {
        super.onPause();
        // False when the activity is not in foreground
        PrefUtils.saveIsForeground( ac, false );
        // Close databases
        closeDatabases();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register to event bus
        EventBus.getDefault().register( this );
        // Set listener for preferences
        PrefUtils.registerSPListener( ac, this );
        // Creates the pub sub strategy for nearby
        PUB_SUB_STRATEGY = new Strategy.Builder()
                .setTtlSeconds( PrefUtils.getPromotionsTime( ac ) ).build();
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
        PrefUtils.unregisterSPListener( ac, this );
        // Disconnect the api client if there is a connection
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            PrefUtils.setSubscribing( ac, false );
            unsubscribe();
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState ) {
        super.onPostCreate( savedInstanceState );
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        if( mDrawerToggle.onOptionsItemSelected( item ) ) {
            return true;
        }

        switch( item.getItemId() ) {
            case R.id.action_settings:
                Intent intent = new Intent( ac, SettingsActivity.class );
                startActivity( intent );
                return true;

            case R.id.action_about:
                mAboutOption.execute();
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

        // Global GUI Components
        mAccountNumber     = (TextView) findViewById( R.id.accountNumberText );
        mAccountDate       = (TextView) findViewById( R.id.accountDateText );
        mAccountBalance    = (TextView) findViewById( R.id.accountBalanceText );
        mAdvertisingLayout = (RelativeLayout) findViewById( R.id.advertisingLayout );
        mAdvertisingImage  = (ImageViewTouch) findViewById( R.id.advertisingImage );
        mDrawerLayout      = (DrawerLayout) findViewById(R.id.drawerLayout);
        ibSubscription     = (ImageButton) findViewById( R.id.ibSubscription );

        // Global Options (main window)
        mPaymentOption       = new PaymentOption( this );
        mSaveCouponOption    = new SaveCouponOption( this );
        mAboutOption         = new AboutOption( this );

        // Global options (navigation window)
        mBalanceOption       = new BalanceOption( this );
        mLinkCodeOption      = new LinkCodeOption( this );
        mLinkAccountOption   = new LinkAccountOption( this );
        mDeLinkAccountOption = new DeLinkAccountOption( this );
        mCloseAccountOption  = new CloseAccountOption( this );

        // Images fit parent
        mAdvertisingImage.setDisplayType( ImageViewTouchBase.DisplayType.FIT_TO_SCREEN );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );

        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close ) {
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

        mAdvertisingImage.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                boolean writePermission = SystemUtils.requestPermission(
                        MainActivity.this,
                        R.string.message_permission_write_external_storage,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                );

                final Drawable drawable = mAdvertisingImage.getDrawable();

                if( !writePermission || drawable == null )
                    return false;

                mSaveCouponOption.execute();
                return true;
            }
        });

        if( PrefUtils.isFirstLogin( ac ) ) {
            mDrawerLayout.openDrawer( GravityCompat.START );
            PrefUtils.saveFirstLogin( ac, false );
        }

        // Show the terms, if the app is updated
        EulaUtils.show( this );
        // Upon orientation change, ensure that the state of the UI is maintained.
        updateUI();
    }

    /**
     * Saves an image as a coupon in the storage and database
     * @param image The file where the image is saved
     */
    public void saveCoupon( File image ) {
        final Drawable drawable = mAdvertisingImage.getDrawable();
        Bitmap bitmap = GUIUtils.drawableToBitmap( drawable );

        try {
            FileOutputStream outStream = new FileOutputStream( image );
            bitmap.compress( Bitmap.CompressFormat.PNG, 90, outStream );

            outStream.flush();
            outStream.close();
            couponsdb.createCoupon( image.getPath(), mMerchant );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the UI when the state of a subscription or
     * publication action changes.
     */
    private void updateUI() {
        Boolean subscriptionTask = PrefUtils.isSubscribing( ac );
        ibSubscription.setImageResource(
                subscriptionTask ? R.drawable.ic_cancel : R.drawable.ic_nearby
        );
        if( !subscriptionTask )
            removeAdvertisement();
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        // Gets the hardware token - account identifier
        hardwareToken = PrefUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
        // Set the account number and current date
        mAccountNumber.setText( hardwareToken );
        mAccountDate.setText( FormatUtils.getCurrentDate() );
    }

    private void setTempPIP( String value ) {
        this.tempPIP = value;
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
                if( mMerchant == null ) {
                    mMerchant = new String( message.getContent() );
                    // Called when a message is detectable nearby.
                    SystemUtils.Logger( TAG, "Found: " + mMerchant );
                    handlerMessages.post( mGetAdvertisement );
                }
            }

            @Override
            public void onLost( final Message message ) {
                String temp = new String( message.getContent() );
                if( mMerchant.equals( temp ) ) {
                    // Called when a message is no longer detectable nearby.
                    SystemUtils.Logger( TAG, "Lost: " + mMerchant );
                    removeAdvertisement();
                }
            }
        };
    }

    /**
     * Subscribes to messages from nearby devices. If not successful, attempts to resolve any error
     * related to Nearby permissions by displaying an opt-in dialog. Registers a callback which
     * updates state when the subscription expires.
     */
    private void subscribe() {
        SystemUtils.Logger( TAG, "trying to subscribe" );
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
                            SystemUtils.Logger( TAG, "no longer subscribing" );
                            PrefUtils.setSubscribing( ac, false );
                        }
                    }).build();

            Nearby.Messages.subscribe( mGoogleApiClient, mMessageListener, options )
                    .setResultCallback( new ResultCallback<Status>() {
                        @Override
                        public void onResult( @NonNull Status status ) {
                            if( status.isSuccess() ) {
                                SystemUtils.Logger( TAG, "subscribed successfully" );
                                PrefUtils.setSubscribing( ac, true );
                            } else {
                                SystemUtils.Logger( TAG, "could not subscribe" );
                                PrefUtils.setSubscribing( ac, false );
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
        SystemUtils.Logger( TAG, "trying to unsubscribe" );
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
                                SystemUtils.Logger( TAG, "unsubscribed successfully" );
                                PrefUtils.setSubscribing( ac, false );
                            } else {
                                SystemUtils.Logger( TAG, "could not unsubscribe" );
                                PrefUtils.setSubscribing( ac, true );
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
        SystemUtils.Logger( TAG, "processing error, status = " + status );
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
        if( PrefUtils.isSubscribing( ac ) ) {
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
        if( !PrefUtils.isSubscribing( ac ) ) {
            PrefUtils.setSubscribing( ac, true );
        } else {
            PrefUtils.setSubscribing( ac, false );
        }
        executePendingSubscriptionTask();
    }

    /**
     * Asks for the PIP to realize a payment
     * @param v The view, not used
     */
    public void paymentClick( View v ) {
        mPaymentOption.execute();
    }

    /**
     * Implements the behavior of the PaymentOption
     * @param inputBox The TextView with the PIP
     */
    public void payment( EditText inputBox ) {
        mAdvertisingLayout.setVisibility( View.GONE );

        // Set a temporary PIP
        setTempPIP( inputBox.getText().toString() );
        originalCode = tempPIP + SKS_SEP + hardwareToken + SKS_SEP;

        ProgressDialogHelper.getInstance().createProgressDialog( ac );
        mRequestManager.invoke( new AuthenticateRequest(
                AUTH_REQ,
                hardwareToken,
                tempPIP
        ) );
    }

    /**
     * List of saved coupons
     * @param v, not used
     */
    public void couponsClick( View v ) {
        Intent intent = new Intent( MainActivity.this, CouponsActivity.class );
        startActivity( intent );
    }

    /**
     * Future feature
     * @param v, not used
     */
    public void networkClick( View v ) {
        Snackbar.make( mDrawerLayout, R.string.no_available, Snackbar.LENGTH_SHORT ).show();
    }

    /**
     * Changes the current PIP
     * @param v, not used
     */
    public void resetPipClick( View v ) {
        mDrawerLayout.closeDrawers();
        Intent intent = new Intent( MainActivity.this, ResetPIPActivity.class );
        startActivity( intent );
    }

    /**
     * Get the saved receipts
     * @param v, not used
     */
    public void savedReceiptsClick( View v ) {
        mDrawerLayout.closeDrawers();
        Intent intent = new Intent( MainActivity.this, ReceiptsActivity.class );
        startActivity( intent );
    }

    /**
     * Link accounts main options
     * @param v, not used
     */
    public void linkAccountsClick( View v ) {
        mDrawerLayout.closeDrawers();

        // Values of the select dialog
        final String title = getString( R.string.linking_menu );
        final String[] options = getResources().getStringArray( R.array.link_options_array );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, final int item ) {
                switch( item ) {
                    // Generates a linking code
                    case 0:
                        mLinkCodeOption.execute();
                        break;

                    // Links accounts with a linking code
                    case 1:
                        mLinkAccountOption.execute();
                        break;

                    // DeLinks an account
                    case 2:
                        mDeLinkAccountOption.execute();
                        break;
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                options,
                onClick
        );
    }

    /**
     * Implements the behavior of the LinkCodeOption
     * @param inputBox The TextView with the PIP
     */
    public void linkCode( EditText inputBox ) {
        final String pip = inputBox.getText().toString();

        ProgressDialogHelper.getInstance().createProgressDialog( ac );
        mRequestManager.invoke( new QueryRequest(
                QUERY_LNK_REQ,
                hardwareToken,
                pip,
                QueryRequest.Record.LINKING_CODE
        ) );
    }

    /**
     * Implements the behavior of the LinkAccountOption
     * @param inputBox The TextView with the linking code
     */
    public void linkAccount( EditText inputBox ) {
        final String linkingCode = inputBox.getText().toString();

        ProgressDialogHelper.getInstance().createProgressDialog( ac );
        mRequestManager.invoke( new LinkRequest(
                LINK_REQ,
                hardwareToken,
                linkingCode
        ) );
    }

    /**
     * Implements the behavior of the DeLinkAccountOption
     * @param inputBox  The TextView with the PIP
     */
    public void deLinkAccount( EditText inputBox ) {
        setTempPIP( inputBox.getText().toString() );

        ProgressDialogHelper.getInstance().createProgressDialog( ac );
        mRequestManager.invoke( new QueryRequest(
                QUERY_LNK_ACC_DEL_REQ,
                hardwareToken,
                tempPIP,
                QueryRequest.Record.LINKED_ACCOUNTS
        ) );
    }

    /**
     * Get the user balance
     * @param v, The button view, not used
     */
    public void balanceClick( View v ) {
        mDrawerLayout.closeDrawers();
        mBalanceOption.execute();
    }

    /**
     * Implements the behavior of the BalanceOption
     * @param inputBox The TextView with the PIP
     */
    public void balance( EditText inputBox ) {
        final String pip = inputBox.getText().toString();

        ProgressDialogHelper.getInstance().createProgressDialog( ac );
        mRequestManager.invoke( new QueryRequest(
                QUERY_BAL_REQ,
                hardwareToken,
                pip
        ) );
    }

    /**
     * Closes the client account
     * @param v, not used
     */
    public void closeAccountClick(View v) {
        mDrawerLayout.closeDrawers();
        mCloseAccountOption.execute();
    }

    /**
     * Implements the behavior of the CloseAccountOption
     * @param inputBox The TextView with the PIP
     */
    public void closeAccount( EditText inputBox ) {
        final String pip = inputBox.getText().toString();

        ProgressDialogHelper.getInstance().createProgressDialog( ac );
        mRequestManager.invoke( new CloseRequest(
                CLOSE_REQ,
                hardwareToken,
                pip
        ) );
    }

    /**
     * Shows the SKS with heart or normal transaction
     * @param v The View contains the type of transaction
     */
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
            sksDialog = new Dialog( this );

            sksDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
            sksDialog.setContentView( R.layout.dialog_sks );
            sksDialog.setCancelable( false );

            // brightness
            final WindowManager.LayoutParams lp = getWindow().getAttributes();
            final float brightnessNow = lp.screenBrightness;

            sksDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                @Override
                public void onShow( DialogInterface dialog ) {
                    lp.screenBrightness = 100 / 100.0f;
                    getWindow().setAttributes( lp );
                }
            });

            sksDialog.setOnKeyListener( new Dialog.OnKeyListener() {
                @Override
                public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) {
                    if( keyCode == KeyEvent.KEYCODE_BACK )
                        dialog.dismiss();
                    return true;
                }
            });

            sksDialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss( DialogInterface dialog ) {
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
        final String totalAmount    = FormatUtils.truncateDecimal( params.get( ServerResponse.AMOUNT ) );
        final String tenderAmount   = FormatUtils.truncateDecimal( params.get( ServerResponse.TAMOUNT ) );
        final String cashBackAmount = FormatUtils.truncateDecimal( params.get( ServerResponse.CASHBACK ) );
        final String balance        = FormatUtils.truncateDecimal( params.get( ServerResponse.BALANCE ) );
        final String currency       = params.get( ServerResponse.CURRENCY ); // Account currency
        final String donor          = params.get( ServerResponse.DONOR );
        final String receiver       = params.get( ServerResponse.RECEIVER );
        final String created        = params.get( ServerResponse.CREATED);

        descriptionText.setText( description );
        authNumberText.setText( authNumber );
        createdText.setText( FormatUtils.UTCtoCurrent( ac, created ) );
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
    public void onResponse( int responseCode, ServerResponse response ) {
        if( responseCode != QUERY_ADV_REQ )
            ProgressDialogHelper.getInstance().destroyProgressDialog();

        String code = response.getCode();
        String message = response.getMessage();
        String to, from;

        switch( responseCode ) {
            case AUTH_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    originalCode += response.getRTime();

                    ProgressDialogHelper.getInstance().createProgressDialog( ac );
                    mRequestManager.invoke( new QueryRequest(
                            QUERY_LNK_ACC_SKS_REQ,
                            hardwareToken,
                            this.tempPIP,
                            QueryRequest.Record.LINKED_ACCOUNTS
                    ) );
                } else {
                    mAdvertisingLayout.setVisibility( View.VISIBLE );
                    YodoHandler.sendMessage( handlerMessages, code, message );
                }

                break;

            case QUERY_BAL_REQ:
                switch( code ) {
                    case ServerResponse.AUTHORIZED_BALANCE:
                        final String tvBalance =
                                FormatUtils.truncateDecimal( response.getParam( ServerResponse.BALANCE ) ) + " " +
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
                        YodoHandler.sendMessage( handlerMessages, code, message );
                        break;
                }
                break;

            case QUERY_ADV_REQ:
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
                                    SystemUtils.Logger( TAG, "Image Load Error: " + error.getMessage() );
                                }
                            }
                        );
                    }
                }
                break;

            case QUERY_LNK_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String linking_code = response.getParam( ServerResponse.LINKING_CODE );

                    Dialog dialog = new Dialog( ac );
                    dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
                    dialog.setContentView( R.layout.dialog_linking_code );

                    final TextView codeText   = (TextView) dialog.findViewById( R.id.codeText );
                    ImageView codeImage = (ImageView) dialog.findViewById( R.id.copyCodeImage );
                    codeText.setText( linking_code );

                    codeImage.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GUIUtils.copyCode( ac, codeText.getText().toString() );
                            ToastMaster.makeText( ac, R.string.copied_text, Toast.LENGTH_SHORT ).show();
                        }
                    });

                    dialog.show();
                } else {
                    YodoHandler.sendMessage( handlerMessages, code, message );
                }
                break;

            case QUERY_LNK_ACC_DEL_REQ:
                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        from = response.getParam( ServerResponse.FROM );
                        to = response.getParam( ServerResponse.TO );

                        Intent i = new Intent( ac, DeLinkActivity.class );
                        i.putExtra( Intents.LINKED_ACC_TO, to );
                        i.putExtra( Intents.LINKED_ACC_FROM, from );
                        i.putExtra( Intents.LINKED_PIP, this.tempPIP );
                        startActivity( i );
                        break;

                    // We don't have links
                    case ServerResponse.ERROR_FAILED:
                        message = getString( R.string.error_message_no_links );
                        YodoHandler.sendMessage( handlerMessages, code, message );
                        break;

                    // If it is something else, show the error
                    default:
                        message = response.getMessage();
                        YodoHandler.sendMessage( handlerMessages, code, message );
                        break;
                }

                setTempPIP( null );
                break;

            case QUERY_LNK_ACC_SKS_REQ:
                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        from = response.getParam( ServerResponse.FROM );
                        // If we have a link show the options
                        if( !from.isEmpty() ) {
                            LayoutInflater inflater = ( LayoutInflater ) getSystemService( LAYOUT_INFLATER_SERVICE );
                            View layout = inflater.inflate( R.layout.dialog_payment, new LinearLayout( ac ), false );
                            alertDialog = AlertDialogHelper.showAlertDialog( ac, layout, getString( R.string.linking_menu ) );
                        }
                        // We are only acting as donor, so show normal SKS
                        else {
                            showSKSDialog( originalCode, null );
                            originalCode = null;
                        }

                        break;

                    // We don't have links
                    case ServerResponse.ERROR_FAILED:
                        showSKSDialog( originalCode, null );
                        originalCode = null;
                        break;

                    // If it is something else, show the error
                    default:
                        YodoHandler.sendMessage( handlerMessages, code, message );
                        break;
                }

                setTempPIP( null );
                break;

            case LINK_REQ:
                YodoHandler.sendMessage( handlerMessages, code, message );
                break;

            case CLOSE_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    PrefUtils.clearPrefConfig( ac );

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
                    YodoHandler.sendMessage( handlerMessages, code, message );
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
        SystemUtils.Logger( TAG, "GoogleApiClient connected" );
        executePendingSubscriptionTask();
    }

    @Override
    public void onConnectionSuspended( int cause ) {
        SystemUtils.Logger( TAG, "GoogleApiClient connection suspended: " +  cause );
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult connectionResult ) {
        SystemUtils.Logger( TAG, "connection to GoogleApiClient failed" );
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
                PrefUtils.setSubscribing( ac, false );
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
