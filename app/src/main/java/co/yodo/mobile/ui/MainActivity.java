package co.yodo.mobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.broadcastreceiver.HeartbeatReceiver;
import co.yodo.mobile.component.SKSCreater;
import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.database.CouponsDataSource;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.database.model.Receipt;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.EulaUtils;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.manager.PromotionManager;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.dialog.PaymentDialog;
import co.yodo.mobile.ui.dialog.ReceiptDialog;
import co.yodo.mobile.ui.dialog.SKSDialog;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.factory.OptionsFactory;
import co.yodo.mobile.ui.tutorial.IntroActivity;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MainActivity extends AppCompatActivity implements
        ApiClient.RequestsListener,
        PromotionManager.IPromotionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifier */
    private String mHardwareToken;

    /** Messages Handler */
    private YodoHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    ApiClient mRequestManager;

    /** Object used to encrypt information */
    @Inject
    RSACrypt mEncrypter;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper mProgressManager;

    /** GUI Controllers */
    @BindView( R.id.llAccountData )
    LinearLayout llAccountData;

    @BindView( R.id.tvAccountNumber )
    TextView tvAccountNumber;

    @BindView( R.id.tvAccountDate )
    TextView tvAccountDate;

    @BindView( R.id.tvAccountBalance )
    TextView tvAccountBalance;

    @BindView( R.id.ivtAdvertising )
    ImageViewTouch ivtAdvertising;

    @BindView( R.id.ibSubscription )
    ImageButton ibSubscription;

    @BindView( R.id.dlPayment )
    DrawerLayout dlPayment;

    /** Handles the drawable layout and toolbar */
    private ActionBarDrawerToggle mDrawerToggle;

    /** Options */
    private OptionsFactory mOptFactory;

    /** Database and current merchant */
    private CouponsDataSource couponsdb;
    private ReceiptsDataSource receiptsdb;
    private String mMerchant;

    /** A {@link MessageListener} for processing messages from nearby devices. */
    private MessageListener mMessageListener;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager mPromotionManager;

    /** SKS time to dismiss */
    private static final int TIME_TO_DISMISS_SKS = 1000 * 60; // 60 seconds

    /** Time between advertisement requests */
    private static final int DELAY_BETWEEN_REQUESTS = 1000 * 25; // 25 seconds

    /** Header and SKS data separator */
    private static final String HDR_SEP = ",";
    private static final String SKS_SEP = "**";

    /** PIP temporal */
    private String tempPIP;

    /** TIP temporal */
    private Integer tempTIP;

    /** Temporal SKSDialog */
    private SKSDialog tempSKS;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    /** Response codes for the server requests */
    private static final int QUERY_LNK_ACC_SKS_REQ = 0x00;
    private static final int QUERY_ADV_REQ         = 0x01;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( this );
        setContentView( R.layout.activity_main );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // True when the activity is in foreground
        PrefUtils.saveIsForeground( ac, true );

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

        // Register listener for requests
        mRequestManager.setListener( this );
    }

    @Override
    public void onStop() {
        // unsubscribe to the promotions
        PrefUtils.setSubscribing( ac, false );

        // Unregister listener for preferences
        PrefUtils.unregisterSPListener( ac, this );

        // Unregister from event bus
        EventBus.getDefault().unregister( this );
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
                mOptFactory.getOption( OptionsFactory.Option.ABOUT ).execute();
                return true;
        }
        return super.onOptionsItemSelected( item );
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
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // get the context
        ac = MainActivity.this;
        mHandlerMessages = new YodoHandler( this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Options
        mOptFactory = new OptionsFactory( this, mHandlerMessages );

        // Setup the toolbar
        Toolbar toolbar = GUIUtils.setActionBar( this, R.string.title_activity_main );

        // Set up the listeners for the drawable and Nearby messages
        initializeDrawableListener( toolbar );
        initializeMessageListener();

        // Setup promotion manager and starts it
        mPromotionManager = new PromotionManager( this, mMessageListener );
        mPromotionManager.startService();

        // Get database objects
        couponsdb  = CouponsDataSource.getInstance( ac );
        receiptsdb = ReceiptsDataSource.getInstance( ac );

        // Images fit parent and set listener to save coupon
        ivtAdvertising.setDisplayType( ImageViewTouchBase.DisplayType.FIT_TO_SCREEN );
        ivtAdvertising.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                boolean writePermission = SystemUtils.requestPermission(
                        MainActivity.this,
                        R.string.message_permission_write_external_storage,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                );

                final Drawable drawable = ivtAdvertising.getDrawable();

                if( !writePermission || drawable == null )
                    return false;

                mOptFactory.getOption( OptionsFactory.Option.SAVE_COUPON ).execute();
                return true;
            }
        });

        // If it is the first login show the drawer open
        if( PrefUtils.isFirstLogin( ac ) ) {
            dlPayment.openDrawer( GravityCompat.START );

            Intent intent = new Intent( ac, IntroActivity.class );
            startActivity( intent );

            PrefUtils.saveFirstLogin( ac, false );
        }

        // Show the terms, if the app is updated
        EulaUtils.show( this );

        // Upon orientation change, ensure that the state of the UI is maintained.
        updateUI();
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        // Gets the hardware token - account identifier
        mHardwareToken = PrefUtils.getHardwareToken( ac );
        if( mHardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }

        // Set the account number and current date
        tvAccountNumber.setText( mHardwareToken );
        tvAccountDate.setText( FormatUtils.getCurrentDate() );
        tvAccountBalance.setText( PrefUtils.getCurrentBalance( ac ) );
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
     * Saves an image as a coupon in the storage and database
     * @param image The file where the image is saved
     */
    public void saveCoupon( File image ) {
        final Drawable drawable = ivtAdvertising.getDrawable();
        final CharSequence description = ivtAdvertising.getContentDescription();
        Bitmap bitmap = GUIUtils.drawableToBitmap( drawable );

        try {
            FileOutputStream outStream = new FileOutputStream( image );
            bitmap.compress( Bitmap.CompressFormat.PNG, 80, outStream );

            outStream.flush();
            outStream.close();
            couponsdb.createCoupon( image.getPath(), description.toString() );
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a temporary PIP, it must be return to null later
     * @param value The value for the temporary PIP
     */
    private void setTempPIP( String value ) {
        this.tempPIP = value;
    }

    /**
     * Sets the temporary TIP, it must be return to null later
     * @param value The value for the temporary TIP
     */
    private void setTempTIP( Integer value ) {
        this.tempTIP = value;
    }

    /**
     * Sets a temporary SKS, it must be return to null later
     * @param value The value for the temporary SKS
     */
    private void setTempSKS( SKSDialog value ) {
        this.tempSKS = value;
    }

    /**
     * Initializes the drawable for the nearby API
     */
    private void initializeDrawableListener( Toolbar toolbar ) {
        mDrawerToggle = new ActionBarDrawerToggle( this, dlPayment, toolbar, R.string.drawer_open, R.string.drawer_close ) {
            @Override
            public void onDrawerClosed( View drawerView ) {
                super.onDrawerClosed( drawerView );
            }
            @Override
            public void onDrawerOpened( View drawerView ) {
                super.onDrawerOpened( drawerView );
            }
        };

        dlPayment.addDrawerListener( mDrawerToggle );
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
                    mHandlerMessages.post( mGetAdvertisement );
                    mPromotionManager.unsubscribe();
                }
            }

            @Override
            public void onLost( final Message message ) {
                final String temp = new String( message.getContent() );
                if( mMerchant.equals( temp ) ) {
                    // Called when a message is no longer detectable nearby.
                    SystemUtils.Logger( TAG, "Lost: " + mMerchant );
                    removeAdvertisement();
                }
            }
        };
    }

    /**
     * Stops the advertisement requests
     */
    private void removeAdvertisement() {
        mMerchant = null;
        mHandlerMessages.removeCallbacks( mGetAdvertisement );
        ivtAdvertising.setImageDrawable( null );
    }

    // Runnable that takes care of request the advertisement
    private Runnable mGetAdvertisement = new Runnable() {
        @Override
        public void run() {
            if( mMerchant != null ) {
                mRequestManager.invoke( new QueryRequest(
                        QUERY_ADV_REQ,
                        mHardwareToken,
                        mMerchant,
                        QueryRequest.Record.ADVERTISING
                ) );
                ivtAdvertising.setContentDescription( mMerchant );

                // Wait some time for the next advertisement request
                mHandlerMessages.postDelayed( mGetAdvertisement, DELAY_BETWEEN_REQUESTS );
            }
        }
    };

    /**
     * Button Actions.
     * {{ ==============================================
     */

    /**
     * Hides the user data
     * @param v, Button view, not used
     */
    public void hideClick( View v ) {
        int visibility = llAccountData.getVisibility();
        if( visibility == View.VISIBLE )
            llAccountData.setVisibility( View.GONE );
        else
            llAccountData.setVisibility( View.VISIBLE );
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
    }

    /**
     * Invokes a pending task based on the subscription state.
     */
    private void executePendingSubscriptionTask() {
        if( PrefUtils.isSubscribing( ac ) ) {
            mPromotionManager.subscribe();
        } else {
            mPromotionManager.unsubscribe();
        }
    }

    /**
     * Asks for the PIP to realize a payment
     * @param v The view, not used
     */
    public void paymentClick( View v ) {
        mOptFactory.getOption( OptionsFactory.Option.PAYMENT ).execute();
    }

    public void payment( String pip, int tip ) {
        // Set temporal pip and tip
        setTempPIP( pip );
        setTempTIP( tip );

        mProgressManager.createProgressDialog( ac );
        mRequestManager.invoke( new QueryRequest(
                QUERY_LNK_ACC_SKS_REQ,
                mHardwareToken,
                pip,
                QueryRequest.Record.LINKED_ACCOUNTS
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
        Snackbar.make( dlPayment, R.string.no_available, Snackbar.LENGTH_SHORT ).show();
    }

    /**
     * Changes the current PIP
     * @param v, not used
     */
    public void resetPipClick( View v ) {
        dlPayment.closeDrawers();
        Intent intent = new Intent( MainActivity.this, ResetPIPActivity.class );
        startActivity( intent );
    }

    /**
     * Get the saved receipts
     * @param v, not used
     */
    public void savedReceiptsClick( View v ) {
        dlPayment.closeDrawers();
        Intent intent = new Intent( MainActivity.this, ReceiptsActivity.class );
        startActivity( intent );
    }

    /**
     * Link accounts main options
     * @param v, not used
     */
    public void linkAccountsClick( View v ) {
        dlPayment.closeDrawers();

        // Values of the select dialog
        final String[] options = getResources().getStringArray( R.array.link_options_array );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, final int item ) {
                switch( item ) {
                    // Generates a linking code
                    case 0:
                        mOptFactory.getOption( OptionsFactory.Option.LINK_CODE ).execute();
                        break;

                    // Links accounts with a linking code
                    case 1:
                        mOptFactory.getOption( OptionsFactory.Option.LINK_ACCOUNT ).execute();
                        break;

                    // DeLinks an account
                    case 2:
                        mOptFactory.getOption( OptionsFactory.Option.DE_LINK_ACCOUNT ).execute();
                        break;
                }
            }
        };

        AlertDialogHelper.create(
                ac,
                R.string.linking_menu,
                options,
                onClick
        ).show();
    }

    /**
     * Get the user balance
     * @param v, The button view, not used
     */
    public void balanceClick( View v ) {
        dlPayment.closeDrawers();
        mOptFactory.getOption( OptionsFactory.Option.BALANCE ).execute();
    }

    public void setBalance( String balance ) {
        PrefUtils.saveBalance( ac, balance );
        tvAccountBalance.setText( balance );
    }

    /**
     * Closes the client account
     * @param v, not used
     */
    public void closeAccountClick(View v) {
        dlPayment.closeDrawers();
        mOptFactory.getOption( OptionsFactory.Option.CLOSE_ACCOUNT ).execute();
    }

    public void clearSavedData() {
        PrefUtils.clearPrefConfig( ac );
        couponsdb.delete();
        receiptsdb.delete();
        SystemUtils.deleteDir(
                new File( Environment.getExternalStorageDirectory(), AppConfig.COUPONS_FOLDER )
        );
    }

    /**
     * Method to show the dialog containing the SKS code
     */
    private void showSKSDialog( final String code, final String payment_type ) {
        final String header = payment_type + HDR_SEP + this.tempTIP;
        final Bitmap sksCode = SKSCreater.createSKS( this, header, mEncrypter.encrypt( code ) );
        setTempSKS( new SKSDialog.Builder( ac )
                .code( sksCode )
                .brightness( 1.0f )
                .dismiss( TIME_TO_DISMISS_SKS )
                .dismissKey( KeyEvent.KEYCODE_BACK )
                .build()
        );

        // It should fix the problem with the delay in the receipts
        ac.sendBroadcast( new Intent( ac, HeartbeatReceiver.class ) );
    }

    /**
     * BUild the receipt from the params (ServerResponse) obtained through GCM
     * @param receipt A receipt to be displayed
     */
    private void buildReceiptDialog( final Receipt receipt ) {
        // Buttons
        final View.OnClickListener saveClick = new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                // Dismiss SKS and receipt
                if( tempSKS != null && mHardwareToken.equals( receipt.getRecipientAccount() ) ) {
                    tempSKS.dismiss();
                    setTempSKS( null );
                }

                // Save the receipt
                receiptsdb.addReceipt( receipt );
                // Show a notification which can reverse the save
                Snackbar.make( dlPayment, R.string.saved_receipt, Snackbar.LENGTH_LONG )
                        .setAction( R.string.message_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                v.setEnabled( false );
                                receiptsdb.deleteReceipt( receipt );
                                buildReceiptDialog( receipt );
                            }
                        } ).show();
            }
        };

        final View.OnClickListener deleteClick = new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                // Dismiss SKS and receipt
                if( tempSKS != null && mHardwareToken.equals( receipt.getRecipientAccount() ) ) {
                    tempSKS.dismiss();
                    setTempSKS( null );
                }

                // Show a notification which can reverse the delete
                Snackbar.make( dlPayment, R.string.message_deleted_receipt, Snackbar.LENGTH_LONG )
                        .setAction( R.string.message_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                v.setEnabled( false );
                                buildReceiptDialog( receipt );
                            }
                        } ).show();
            }
        };

        new ReceiptDialog.Builder( ac )
                .description( receipt.getDescription() )
                .created( receipt.getCreated() )
                .total( receipt.getTotalAmount(), receipt.getTCurrency() )
                .authnumber( receipt.getAuthnumber() )
                .donor( receipt.getDonorAccount() )
                .recipient( receipt.getRecipientAccount() )
                .tender( receipt.getTenderAmount(), receipt.getDCurrency() )
                .cashback( receipt.getCashbackAmount(), receipt.getTCurrency() )
                .save( saveClick )
                .delete( deleteClick )
                .build();

        // Account balance
        setBalance( String.format( "%s %s",
                FormatUtils.truncateDecimal( receipt.getBalanceAmount() ),
                receipt.getCurrency()
        ) );
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        if( responseCode != QUERY_ADV_REQ )
            mProgressManager.destroyProgressDialog();

        String code = response.getCode();
        String message = response.getMessage();

        switch( responseCode ) {
            case QUERY_ADV_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String url = response.getParam( ServerResponse.ADVERTISING ).replaceAll( " ", "%20" );
                    if( !url.isEmpty() ) {
                        mRequestManager.getImageLoader().get( url, new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse( ImageLoader.ImageContainer response, boolean isImmediate ) {
                                    if( response.getBitmap() != null && mMerchant != null ) {
                                        // load image into ImageView
                                        ivtAdvertising.setImageBitmap( response.getBitmap() );
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

            case QUERY_LNK_ACC_SKS_REQ:
                // SKS - User data
                final String originalCode =
                        tempPIP        + SKS_SEP +
                        mHardwareToken + SKS_SEP +
                        response.getRTime();

                // Identifier for a normal payment
                final String yodoPayment = getString( R.string.account_yodo );

                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        String from = response.getParam( ServerResponse.FROM );
                        // If we have a link show the options
                        if( !from.isEmpty() ) {
                            View.OnClickListener onClick = new View.OnClickListener() {
                                @Override
                                public void onClick( View v ) {
                                    final ImageView accountImage = (ImageView) v;
                                    final String paymentType = accountImage.getContentDescription().toString();
                                    showSKSDialog( originalCode, paymentType );
                                }
                            };

                            new PaymentDialog.Builder( ac )
                                    .cancelable( true )
                                    .action( onClick )
                                    .build();
                        }
                        // We are only acting as donor, so show normal SKS
                        else {
                            showSKSDialog( originalCode, yodoPayment );
                        }

                        break;

                    // We don't have links
                    case ServerResponse.ERROR_FAILED:
                        showSKSDialog( originalCode, yodoPayment );
                        break;

                    // If it is something else, show the error
                    default:
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }

                // Set PIP and TIP to null
                setTempPIP( null );
                //setTempTIP( null );
                break;
        }
    }

    @SuppressWarnings("unused") // receives GCM receipts
    @Subscribe( threadMode = ThreadMode.MAIN )
    public void onResponseEvent( ServerResponse response ) {
        Receipt receipt = new Receipt.Builder()
                .description( response.getParam( ServerResponse.DESCRIPTION ) )
                .authnumber( response.getParam( ServerResponse.AUTHNUMBER ) )
                .created( response.getParam( ServerResponse.CREATED ) )
                .total( response.getParam( ServerResponse.AMOUNT ),
                        response.getParam( ServerResponse.TCURRENCY ) )
                .tender( response.getParam( ServerResponse.TAMOUNT ),
                         response.getParam( ServerResponse.DCURRENCY ) )
                .cashback( response.getParam( ServerResponse.CASHBACK ) )
                .exchRate( response.getParam( ServerResponse.EXCH_RATE ) )
                .donor( response.getParam( ServerResponse.DONOR ) )
                .recipient( response.getParam( ServerResponse.RECEIVER ) )
                .balance( response.getParam( ServerResponse.BALANCE ),
                          response.getParam( ServerResponse.CURRENCY ) )
                .build();

        buildReceiptDialog( receipt );
    }

    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
        if( key.equals( AppConfig.SPREF_SUBSCRIPTION_TASK ) ) {
            executePendingSubscriptionTask();
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            } );
        }
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
}
