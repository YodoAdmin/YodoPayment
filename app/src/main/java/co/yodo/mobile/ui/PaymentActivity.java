package co.yodo.mobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.PromotionManager;
import co.yodo.mobile.business.component.cipher.RSACrypt;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.QueryRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.utils.AppConfig;
import co.yodo.mobile.helper.EulaHelper;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.ProgressDialogHelper;
import co.yodo.mobile.ui.option.SaveCouponOption;
import co.yodo.mobile.ui.option.factory.OptionsFactory;
import co.yodo.mobile.ui.tutorial.IntroActivity;
import co.yodo.mobile.utils.SystemUtils;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import timber.log.Timber;

public class PaymentActivity extends BaseActivity implements
        PromotionManager.IPromotionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    /** The context object */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** Object used to encrypt information */
    @Inject
    RSACrypt cipher;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper progressManager;

    /** GUI Controllers */
    @BindView( R.id.llAccountData )
    LinearLayout llAccountData;

    @BindView( R.id.tvAccountNumber )
    TextView tvAccountNumber;

    @BindView( R.id.tvAccountDate )
    TextView tvAccountDate;

    @BindView( R.id.tvAccountBalance )
    TextView tvAccountBalance;

    @BindView( R.id.ivtPromotion )
    ImageViewTouch ivtPromotion;

    @BindView( R.id.ibSubscription )
    ImageButton ibSubscription;

    @BindView( R.id.layout_payment )
    DrawerLayout dlPayment;

    /** Handles the drawable layout and toolbar */
    private ActionBarDrawerToggle drawerToggle;

    /** Handle all the options of the Payment */
    private OptionsFactory optsFactory;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager promotionManager;

    /** A {@link MessageListener} for processing messages from nearby devices. */
    private MessageListener promotionListener;

    /** Current merchant used by nearby */
    private String currentMerchant;

    /** Time between advertisement requests */
    private static final int DELAY_BETWEEN_REQUESTS = 1000 * 25; // 25 seconds

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    /** Handler and runnable that takes care of request the promotions */
    private Handler handler = new Handler();
    private Runnable getPromotions = new Runnable() {
        @Override
        public void run() {
            if( currentMerchant != null ) {
                requestManager.invoke(
                        new QueryRequest(uuidToken, currentMerchant, QueryRequest.Record.ADVERTISING ),
                        new ApiClient.RequestCallback() {
                            @Override
                            public void onResponse( ServerResponse response ) {
                                final String code = response.getCode();
                                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                                    String url = response.getParams().getAdvertisingImage().replaceAll( " ", "%20" );
                                    if( !url.isEmpty() ) {
                                        Picasso.with( context )
                                                .load( url )
                                                .error( R.drawable.ic_no_image )
                                                .into( ivtPromotion );
                                    }
                                }
                            }

                            @Override
                            public void onError( String message ) {
                            }
                        }
                );
                ivtPromotion.setContentDescription( currentMerchant );
                handler.postDelayed( getPromotions, DELAY_BETWEEN_REQUESTS );
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupGUI(savedInstanceState);
        Timber.i(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set listener for preferences
        PreferencesHelper.registerListener(this );
    }

    @Override
    public void onStop() {
        // unsubscribe to the promotions
        PreferencesHelper.setSubscribing( context, false );

        // Unregister listener for preferences
        PreferencesHelper.unregisterListener(this );
        super.onStop();
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState ) {
        super.onPostCreate( savedInstanceState );
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        if( drawerToggle.onOptionsItemSelected( item ) ) {
            return true;
        }

        switch( item.getItemId() ) {
            case R.id.action_settings:
                Intent intent = new Intent( context, SettingsActivity.class );
                startActivity( intent );
                return true;

            case R.id.action_about:
                optsFactory.getOption( OptionsFactory.Option.ABOUT ).execute();
                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onConnected( @Nullable Bundle bundle ) {
        Timber.i( "GoogleApiClient connected" );
        executePendingSubscriptionTask();
    }

    @Override
    public void onConnectionSuspended( int cause ) {
        Timber.i( "GoogleApiClient connection suspended: " +  cause );
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult connectionResult ) {
        Timber.i( "connection to GoogleApiClient failed" );
    }

    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
        if( key.equals( AppConfig.SPREF_SUBSCRIPTION ) ) {
            executePendingSubscriptionTask();
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            } );
        }

        else if (key.equals(AppConfig.SPREF_BALANCE)) {
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    updateData();
                }
            } );
        }
    }

    @Override
    public void updateData() {
        super.updateData();

        // Set the account number and current date
        tvAccountNumber.setText(uuidToken);
        tvAccountDate.setText( FormatUtils.getCurrentDate() );
        tvAccountBalance.setText( PreferencesHelper.getCurrentBalance() );
    }

    /**
     * Starts a new payment activity instance
     * @param context The application context
     */
    public static void newInstance(Context context) {
        Intent intent = new Intent(context, PaymentActivity.class);
        context.startActivity(intent);
    }

    /**
     * Hides the user data
     * @param v, Button view, not used
     */
    public void hideUserData( View v ) {
        int visibility = llAccountData.getVisibility();
        if (visibility == View.VISIBLE) {
            llAccountData.setVisibility(View.GONE);
        } else {
            llAccountData.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Tries to subscribe to close Rocket (POS) devices
     * for advertisement
     * @param v The view, used to change the icon
     */
    public void startPromotions(View v) {
        if (!PreferencesHelper.isSubscribing(context)) {
            PreferencesHelper.setSubscribing(context, true);
        } else {
            PreferencesHelper.setSubscribing(context, false);
        }
    }

    /**
     * Asks for the PIP to realize a payment
     * @param v The view, not used
     */
    public void payment( View v ) {
        optsFactory.getOption( OptionsFactory.Option.PAYMENT ).execute();
    }

    /**
     * List of saved coupons
     * @param v, not used
     */
    public void coupons( View v ) {
        Intent intent = new Intent( PaymentActivity.this, CouponsActivity.class );
        startActivity( intent );
    }

    /**
     * Future feature
     * @param v, not used
     */
    public void network( View v ) {
        optsFactory.getOption( OptionsFactory.Option.P2P ).execute();
    }

    /**
     * Changes the current PIP
     * @param v, not used
     */
    public void resetPip( View v ) {
        dlPayment.closeDrawers();
        Intent intent = new Intent( context, ResetPipActivity.class );
        startActivity( intent );
    }

    /**
     * Get the saved receipts
     * @param v, not used
     */
    public void receipts( View v ) {
        dlPayment.closeDrawers();
        Intent intent = new Intent( PaymentActivity.this, ReceiptsActivity.class );
        startActivity( intent );
    }

    /**
     * Link accounts main options
     * @param v, not used
     */
    public void linkAccountsClick( View v ) {
        dlPayment.closeDrawers();

        // Values of the select dialog
        final String[] options = getResources().getStringArray( R.array.link_options );
        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, final int item ) {
                switch( item ) {
                    case 0: // Generates a linking code
                        optsFactory.getOption( OptionsFactory.Option.LINKING_CODE ).execute();
                        break;

                    case 1: // Link accounts with a linking code
                        optsFactory.getOption( OptionsFactory.Option.LINK_ACCOUNT ).execute();
                        break;

                    case 2: // List the linking accounts
                        optsFactory.getOption( OptionsFactory.Option.LINKED_ACCOUNTS ).execute();
                        break;
                }
            }
        };

        AlertDialogHelper.show(
                PaymentActivity.this,
                R.string.text_options_select,
                options,
                onClick
        );
    }

    /**
     * Get the user balance
     * @param v, The button view, not used
     */
    public void balance( View v ) {
        dlPayment.closeDrawers();
        optsFactory.getOption( OptionsFactory.Option.BALANCE ).execute();
    }

    /**
     * Closes the client account
     * @param v, not used
     */
    public void closeAccount( View v) {
        dlPayment.closeDrawers();
        optsFactory.getOption( OptionsFactory.Option.CLOSE_ACCOUNT ).execute();
    }

    /**
     * Configures the main GUI Controllers
     */
    @Override
    protected void setupGUI( Bundle savedInstanceState ) {
        super.setupGUI( savedInstanceState );
        // Injection
        YodoApplication.getComponent().inject( this );

        // Show the terms, if the app is updated
        EulaHelper.show( this, null );

        // If it is the first login show the drawer open
        if( PreferencesHelper.isFirstLogin( context ) ) {
            Intent intent = new Intent( context, IntroActivity.class );
            startActivity( intent );

            PreferencesHelper.saveFirstLogin( context, false );
        }

        // Set up the listeners for the drawable
        initializeDrawableListener();

        // Options
        optsFactory = new OptionsFactory( this );

        // Setup promotion manager and starts it
        initializeNearbyListener();
        promotionManager = new PromotionManager( this, promotionListener );
        promotionManager.start();

        // Images fit parent and set listener to save coupon
        ivtPromotion.setDisplayType( ImageViewTouchBase.DisplayType.FIT_TO_SCREEN );
        ivtPromotion.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                boolean writePermission = SystemUtils.requestPermission(
                        PaymentActivity.this,
                        R.string.text_permission_write_external_storage,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                );

                if( writePermission ) {
                    ( (SaveCouponOption) optsFactory.getOption( OptionsFactory.Option.COUPONS ) )
                            .setPromotionImage( ivtPromotion )
                            .execute();
                    return true;
                }
                return false;
            }
        });

        // Upon orientation change, ensure that the state of the UI is maintained.
        updateUI();
    }

    /**
     * Updates the UI when the state of a subscription or
     * publication action changes.
     */
    private void updateUI() {
        Boolean subscriptionTask = PreferencesHelper.isSubscribing( context );
        ibSubscription.setImageResource(
                subscriptionTask ? R.mipmap.ic_cancel : R.mipmap.ic_nearby
        );

        if( !subscriptionTask ) {
            removeAdvertisement();
        }
    }

    /**
     * Initializes the drawable for the nearby API
     */
    private void initializeDrawableListener() {
        drawerToggle = new ActionBarDrawerToggle( this, dlPayment, R.string.text_drawer_open, R.string.text_drawer_close ) {
            @Override
            public void onDrawerClosed( View drawerView ) {
                super.onDrawerClosed( drawerView );
            }
            @Override
            public void onDrawerOpened( View drawerView ) {
                super.onDrawerOpened( drawerView );
            }
        };

        dlPayment.addDrawerListener( drawerToggle );
    }

    /**
     * Initializes the listener for the nearby API
     */
    private void initializeNearbyListener() {
        promotionListener = new MessageListener() {
            @Override
            public synchronized void onFound( final Message message ) {
                if( currentMerchant == null ) {
                    // Called when a message is detectable nearby
                    currentMerchant = new String( message.getContent() );
                    handler.post( getPromotions );
                    promotionManager.unsubscribe();
                }
            }

            @Override
            public void onLost( final Message message ) {
                final String temp = new String( message.getContent() );
                if( currentMerchant.equals( temp ) ) {
                    // Called when a message is no longer detectable nearby.
                    removeAdvertisement();
                }
            }
        };
    }

    /**
     * Stops the advertisement requests
     */
    private void removeAdvertisement() {
        currentMerchant = null;
        handler.removeCallbacks( getPromotions );
        ivtPromotion.setImageDrawable( null );
    }

    /**
     * Invokes a pending task based on the subscription state.
     */
    private void executePendingSubscriptionTask() {
        if( PreferencesHelper.isSubscribing( context ) ) {
            promotionManager.subscribe();
        } else {
            promotionManager.unsubscribe();
        }
    }
}
