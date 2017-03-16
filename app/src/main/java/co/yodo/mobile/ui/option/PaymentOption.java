package co.yodo.mobile.ui.option;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.business.broadcastreceiver.HeartbeatReceiver;
import co.yodo.mobile.business.component.SKSCreater;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.AuthenticateRequest;
import co.yodo.mobile.business.network.request.QueryRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.dialog.PaymentDialog;
import co.yodo.mobile.ui.dialog.PaymentDialog.Payment;
import co.yodo.mobile.ui.dialog.SKSDialog;
import co.yodo.mobile.ui.dialog.contract.IDialog;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.PipUtils;

/**
 * Created by hei on 14/06/16.
 * Implements the Payment Option of the MainActivity
 */
public class PaymentOption extends IRequestOption {
    /** GUI Controllers */
    private SeekBar sbTips;

    /** Temporal */
    private int mTempTip = 0;

    /** Header, SKS data and account separators */
    private static final String HDR_SEP = ",";
    private static final String SKS_SEP = "**";
    private static final String ACC_SEP = "-";

    /** SKS time to dismiss */
    private static final int TIME_TO_DISMISS_SKS = 1000 * 60; // 60 seconds

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public PaymentOption( final BaseActivity activity ) {
        super( activity );

        // Get text for tips
        //mTipText = this.activity.getString( R.string.text_tip );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                if( PipUtils.validate( activity, etInput, null ) ) {
                    final String otp = TOTPUtils.defaultOTP( etInput.getText().toString() );

                    // Start the request
                    progressManager.create( activity );
                    requestManager.invoke(
                            new AuthenticateRequest( hardwareToken, otp ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    progressManager.destroy();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            alertDialog.dismiss();
                                            requestLinkedAccounts( otp );
                                            break;

                                        case ServerResponse.ERROR_INCORRECT_PIP:
                                            tilPip.setError( activity.getString( R.string.error_pip ) );
                                            break;

                                        default:
                                            handleServerError();
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
            }
        };

        setupGUI( layout );

        alertDialog = AlertDialogHelper.create(
                this.activity,
                layout,
                buildOnClick( okClick )
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
        clearGUI();
        //sbTips.setProgress( 0 );
    }

    /**
     * Setups other components for the option
     * @param layout The layout of the option
     */
    private void setupGUI( View layout ) {
        // Set other components
        /*final LinearLayout llTips = (LinearLayout) layout.findViewById( R.id.llTips );
        final TextView tvTips = (TextView) layout.findViewById( R.id.tvTips );
        sbTips = (SeekBar) layout.findViewById( R.id.sbTips );

        llTips.setVisibility( View.VISIBLE );
        tvTips.setText( String.format( mTipText, 0 ) );
        sbTips.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progressValue, boolean fromUser ) {
                tvTips.setText( String.format( mTipText, progressValue ) );
                mTempTip = progressValue;
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
            }
        } );*/
    }

    /**
     * Request the linked accounts (if any) to the server
     * @param otp The user one time password, required for the request
     */
    private void requestLinkedAccounts( final String otp ) {
        progressManager.create( activity );
        requestManager.invoke(
                new QueryRequest( hardwareToken, otp, QueryRequest.Record.LINKED_ACCOUNTS ),
                new ApiClient.RequestCallback() {
                    @Override
                    public void onResponse( ServerResponse response ) {
                        progressManager.destroy();
                        final String code = response.getCode();
                        final String userCode = otp + SKS_SEP + hardwareToken;

                        switch( code ) {
                            case ServerResponse.AUTHORIZED:
                                final String from = response.getParams().getLinkedFrom();

                                // If we have a link show the options
                                if( from != null && !from.isEmpty() ) {
                                    PaymentDialog.OnClickListener onClick = new PaymentDialog.OnClickListener() {
                                        @Override
                                        public void onClick( final Payment type ) {
                                            final String[] accounts = from.split( ACC_SEP );

                                            switch( type ) {
                                                case HEART:
                                                    if( accounts.length > 1 ) {
                                                        /*List<String> list = new ArrayList<>();
                                                        for( String account : accounts ) {
                                                            list.add( PrefUtils.getNickname( account ) );
                                                        }

                                                        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick( DialogInterface dialog, final int item ) {
                                                                final String donor = TOTPUtils.sha1( accounts[item] );
                                                                showSKS( "0", userCode + SKS_SEP + donor, type.getValue() );
                                                                dialog.dismiss();
                                                            }
                                                        };

                                                        AlertDialogHelper.show(
                                                                activity,
                                                                R.string.text_options_select,
                                                                list.toArray( new String[0] ),
                                                                onClick
                                                        );*/
                                                    } else {
                                                        showSKS( "0", userCode, type.getValue() );
                                                    }

                                                    break;

                                                default:
                                                    showSKS( "0", userCode, type.getValue() );
                                                    break;
                                            }
                                        }
                                    };

                                    new PaymentDialog.Builder( activity )
                                            .cancelable( true )
                                            .action( onClick )
                                            .build();
                                }
                                break;

                            case ServerResponse.ERROR_FAILED:
                                showSKS( "0", userCode, Payment.YODO.getValue() );
                                break;

                            default:
                                handleServerError();
                                break;
                        }

                        // SKS - User data
                        /*final String originalCode =
                                tempPIP + SKS_SEP +
                                        hardwareToken;

                        // Identifier for a normal payment
                        final String yodoPayment  = getString( R.string.account_yodo );
                        final String heartPayment = getString( R.string.account_yodo_heart );

                        switch( code ) {
                            case ServerResponse.AUTHORIZED:
                                final String from = response.getParams().getLinkedFrom();

                                // If we have a link show the options
                                if( from != null && !from.isEmpty() ) {
                                    View.OnClickListener onClick = new View.OnClickListener() {
                                        @Override
                                        public void onClick( View v ) {
                                            final ImageView accountImage = (ImageView) v;
                                            final String paymentType = accountImage.getContentDescription().toString();
                                            final String[] accounts = from.split( "-" );

                                            if( paymentType.equals( heartPayment ) && accounts.length > 1 ) {
                                                List<String> list = new ArrayList<>();

                                                for( String account : accounts ) {
                                                    final String nickname = PrefUtils.getNickname( context, account );
                                                    if( nickname != null )
                                                        list.add( nickname );
                                                    else
                                                        list.add( account );
                                                }

                                                DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                                                    public void onClick( DialogInterface dialog, final int item ) {
                                                        final String donor = TOTPUtils.sha1( accounts[item] );
                                                        showSKS( originalCode + SKS_SEP + donor, paymentType );
                                                        dialog.dismiss();
                                                    }
                                                };

                                                AlertDialogHelper.newInstance( context, R.string.linking_menu, list.toArray( new String[0] ), onClick ).show();
                                            } else {
                                                showSKS( originalCode, paymentType );
                                            }
                                        }
                                    };

                                    new PaymentDialog.Builder( context )
                                            .cancelable( true )
                                            .action( onClick )
                                            .build();
                                }
                                // We are only acting as donor, so show normal SKS
                                else {
                                    showSKS( originalCode, yodoPayment );
                                }

                                break;

                            // We don't have links
                            case ServerResponse.ERROR_FAILED:
                                showSKS( originalCode, yodoPayment );
                                break;*/
                    }

                    @Override
                    public void onError( String message ) {
                        handleApiError( message );
                    }
                }
        );
    }

    /**
     * Method to show the dialog containing the SKS code
     * @param tip the tip which is included in the SKS header
     * @param code The code that contains the user data
     * @param paymentType The type of payment (e.g. yodo, heart)
     */
    private void showSKS( String tip, String code, String paymentType ) {
        final String header = paymentType + HDR_SEP + tip;
        final Bitmap sksCode = SKSCreater.createSKS( activity, header, code );
        final IDialog dialog = new SKSDialog.Builder( activity )
                .code( sksCode )
                .brightness( 1.0f )
                .dismiss( TIME_TO_DISMISS_SKS )
                .dismissKey( KeyEvent.KEYCODE_BACK )
                .build();

        // Sets the dialog to the activity for a future dismiss
        activity.setDialog( dialog );

        // It should fix the problem with the delay in the receipts
        activity.sendBroadcast( new Intent( activity, HeartbeatReceiver.class ) );
    }

    /**
     * Just shows an error message
     * We don't know what the error is
     */
    private void handleServerError() {
        ErrorUtils.handleError(
                activity,
                activity.getString( R.string.error_unknown ),
                false
        );
    }

    /**
     * We received an error from the API
     * let's show the correct message
     */
    private void handleApiError( String message ) {
        progressManager.destroy();
        ErrorUtils.handleError(
                activity,
                message,
                false
        );
    }
}
