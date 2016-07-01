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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.io.UnsupportedEncodingException;

import co.yodo.mobile.R;
import co.yodo.mobile.component.SKSCreater;
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
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.AuthenticateRequest;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.dialog.PaymentDialog;
import co.yodo.mobile.ui.dialog.ReceiptDialog;
import co.yodo.mobile.ui.dialog.SKSDialog;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.AboutOption;
import co.yodo.mobile.ui.option.BalanceOption;
import co.yodo.mobile.ui.option.CloseAccountOption;
import co.yodo.mobile.ui.option.DeLinkAccountOption;
import co.yodo.mobile.ui.option.LinkAccountOption;
import co.yodo.mobile.ui.option.LinkCodeOption;
import co.yodo.mobile.ui.option.PaymentOption;
import co.yodo.mobile.ui.option.SaveCouponOption;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MainActivity extends AppCompatActivity implements
        YodoRequest.RESTListener,
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
    private YodoRequest mRequestManager;

    /** GUI Controllers */
    private TextView mAccountNumber;
    private TextView mAccountDate;
    private TextView mAccountBalance;
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

    /** A {@link MessageListener} for processing messages from nearby devices. */
    private MessageListener mMessageListener;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager mPromotionManager;

    /** SKS time to dismiss */
    private static final int TIME_TO_DISMISS_SKS = 1000 * 60; // 60 seconds

    /** Time between advertisement requests */
    private static final int DELAY_BETWEEN_REQUESTS = 1000 * 25; // 25 seconds

    /** SKS data separator */
    private static final String SKS_SEP = "**";

    /** PIP temporal */
    private String tempPIP;

    /** Temporal SKSDialog */
    private SKSDialog tempSKS;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    /** Request code to use when launching the resolution activity. */
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    /** Response codes for the server requests */
    private static final int AUTH_REQ              = 0x00;
    private static final int QUERY_ADV_REQ         = 0x01;
    private static final int QUERY_LNK_ACC_SKS_REQ = 0x02;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
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
        // Register listener for requests and  broadcast receivers
        mRequestManager.setListener( this );
        // Connect to the advertise service
        this.mPromotionManager.startService();
    }

    @Override
    public void onStop() {
        // Unregister from event bus
        EventBus.getDefault().unregister( this );
        // Unregister listener for preferences
        PrefUtils.unregisterSPListener( ac, this );
        // Disconnect the advertise service
        this.mPromotionManager.stopService();
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
        mHandlerMessages = new YodoHandler( MainActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );

        // Global GUI Components
        mAccountNumber     = (TextView) findViewById( R.id.accountNumberText );
        mAccountDate       = (TextView) findViewById( R.id.accountDateText );
        mAccountBalance    = (TextView) findViewById( R.id.accountBalanceText );
        mAdvertisingImage  = (ImageViewTouch) findViewById( R.id.advertisingImage );
        mDrawerLayout      = (DrawerLayout) findViewById(R.id.drawerLayout);
        ibSubscription     = (ImageButton) findViewById( R.id.ibSubscription );

        // Global Options (main window)
        mPaymentOption    = new PaymentOption( this );
        mSaveCouponOption = new SaveCouponOption( this );
        mAboutOption      = new AboutOption( this );

        // Global options (navigation window)
        mBalanceOption       = new BalanceOption( this, mRequestManager, mHandlerMessages );
        mLinkCodeOption      = new LinkCodeOption( this, mRequestManager, mHandlerMessages );
        mLinkAccountOption   = new LinkAccountOption( this, mRequestManager, mHandlerMessages );
        mDeLinkAccountOption = new DeLinkAccountOption( this, mRequestManager, mHandlerMessages );
        mCloseAccountOption  = new CloseAccountOption( this, mRequestManager, mHandlerMessages );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );

        // Set up the listeners for the drawable and Nearby messages
        initializeDrawableListener( toolbar );
        initializeMessageListener();

        // Setup promotion manager
        mPromotionManager = new PromotionManager( this, mMessageListener, REQUEST_RESOLVE_ERROR );

        // Get database objects
        couponsdb  = CouponsDataSource.getInstance( ac );
        receiptsdb = ReceiptsDataSource.getInstance( ac );

        // Images fit parent and set listener to save coupon
        mAdvertisingImage.setDisplayType( ImageViewTouchBase.DisplayType.FIT_TO_SCREEN );
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

        // If it is the first login show the drawer open
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
        mAccountNumber.setText( mHardwareToken );
        mAccountDate.setText( FormatUtils.getCurrentDate() );
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
        final Drawable drawable = mAdvertisingImage.getDrawable();
        final CharSequence description = mAdvertisingImage.getContentDescription();
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
        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close ) {
            @Override
            public void onDrawerClosed( View drawerView ) {
                super.onDrawerClosed( drawerView );
            }
            @Override
            public void onDrawerOpened( View drawerView ) {
                super.onDrawerOpened( drawerView );
            }
        };

        mDrawerLayout.addDrawerListener( mDrawerToggle );
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
        mAdvertisingImage.setImageDrawable( null );
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
                mAdvertisingImage.setContentDescription( mMerchant );

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
        mPaymentOption.execute();
    }

    public void payment( EditText inputBox ) {
        // Stop promotions
        if( PrefUtils.isSubscribing( ac ) )
            mPromotionManager.unsubscribe();

        // Set a temporary PIP and Code
        setTempPIP( inputBox.getText().toString() );

        // Start the request
        ProgressDialogHelper.getInstance().createProgressDialog( ac );
        mRequestManager.invoke( new AuthenticateRequest(
                AUTH_REQ,
                mHardwareToken,
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
     * Get the user balance
     * @param v, The button view, not used
     */
    public void balanceClick( View v ) {
        mDrawerLayout.closeDrawers();
        mBalanceOption.execute();
    }

    public void setBalance( String balance ) {
        this.mAccountBalance.setText( balance );
    }

    /**
     * Closes the client account
     * @param v, not used
     */
    public void closeAccountClick(View v) {
        mDrawerLayout.closeDrawers();
        mCloseAccountOption.execute();
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
    private void showSKSDialog( final String code, final Integer account_type ) {
        try {
            final Bitmap sksCode = SKSCreater.createSKS( code, this, SKSCreater.SKS_CODE, account_type );
            setTempSKS( new SKSDialog.Builder( ac )
                    .code( sksCode )
                    .brightness( 1.0f )
                    .dismiss( TIME_TO_DISMISS_SKS )
                    .dismissKey( KeyEvent.KEYCODE_BACK )
                    .build()
            );

            // It should fix the problem with the delay in the receipts
            ac.sendBroadcast( new Intent( "com.google.android.intent.action.GTALK_HEARTBEAT" ) );
            ac.sendBroadcast( new Intent( "com.google.android.intent.action.MCS_HEARTBEAT" ) );
        } catch( UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
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
                Snackbar.make( mDrawerLayout, R.string.saved_receipt, Snackbar.LENGTH_LONG )
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
                Snackbar.make( mDrawerLayout, R.string.message_deleted_receipt, Snackbar.LENGTH_LONG )
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
        mAccountBalance.setText(
                String.format( "%s %s",
                        FormatUtils.truncateDecimal( receipt.getBalanceAmount() ),
                        receipt.getCurrency()
                )
        );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        if( responseCode != QUERY_ADV_REQ )
            ProgressDialogHelper.getInstance().destroyProgressDialog();

        String code = response.getCode();
        String message = response.getMessage();

        switch( responseCode ) {
            case AUTH_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ProgressDialogHelper.getInstance().createProgressDialog( ac );
                    mRequestManager.invoke( new QueryRequest(
                            QUERY_LNK_ACC_SKS_REQ,
                            mHardwareToken,
                            this.tempPIP,
                            QueryRequest.Record.LINKED_ACCOUNTS
                    ) );
                } else {
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
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

            case QUERY_LNK_ACC_SKS_REQ:
                final String originalCode =
                        tempPIP + SKS_SEP +
                                mHardwareToken + SKS_SEP +
                        response.getRTime();

                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        String from = response.getParam( ServerResponse.FROM );
                        // If we have a link show the options
                        if( !from.isEmpty() ) {
                            View.OnClickListener onClick = new View.OnClickListener() {
                                @Override
                                public void onClick( View v ) {
                                    final ImageView accountImage = (ImageView) v;
                                    Integer account_type = Integer.parseInt(
                                            accountImage.getContentDescription().toString()
                                    );

                                    if( account_type == 0 )
                                        account_type = null;
                                    showSKSDialog( originalCode, account_type );
                                }
                            };

                            new PaymentDialog.Builder( ac )
                                    .cancelable( true )
                                    .action( onClick )
                                    .build();
                        }
                        // We are only acting as donor, so show normal SKS
                        else {
                            showSKSDialog( originalCode, null );
                        }

                        break;

                    // We don't have links
                    case ServerResponse.ERROR_FAILED:
                        showSKSDialog( originalCode, null );
                        break;

                    // If it is something else, show the error
                    default:
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }

                setTempPIP( null );
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
        // Let the manager knows about the result
        this.mPromotionManager.onResolutionResult();
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
