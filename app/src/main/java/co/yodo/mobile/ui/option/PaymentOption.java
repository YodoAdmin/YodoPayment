package co.yodo.mobile.ui.option;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.AuthenticateRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the Payment Option of the MainActivity
 */
public class PaymentOption extends IRequestOption implements ApiClient.RequestsListener {
    /** GUI Controllers */
    private SeekBar sbTips;

    /** Text */
    private final String mTipText;

    /** Temporal */
    private String mTempPIP;
    private int mTempTip = 0;

    /** Response codes for the server requests */
    private static final int AUTH_REQ = 0x00;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public PaymentOption( MainActivity activity, YodoHandler handlerMessages ) {
        super( activity, handlerMessages );

        // Get text for tips
        mTipText = mActivity.getString( R.string.text_tip );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                try {
                    if( mPipValidator.validate( etInput ) ) {
                        mAlertDialog.dismiss();

                        // Set a temporary PIP and Code
                        final String pip = etInput.getText().toString();
                        setTempPIP( pip );

                        // Start the request
                        mProgressManager.createProgressDialog( mActivity );
                        mRequestManager.setListener( PaymentOption.this );
                        mRequestManager.invoke( new AuthenticateRequest(
                                AUTH_REQ,
                                mHardwareToken,
                                mTempPIP
                        ) );
                    }
                } catch( NoSuchFieldException e ) {
                    e.printStackTrace();
                }
            }
        };

        setupGUI( layout );

        mAlertDialog = AlertDialogHelper.create(
                mActivity,
                layout,
                buildOnClick( okClick )
        );
    }

    /**
     * Setups other components for the option
     * @param layout The layout of the option
     */
    @TargetApi( Build.VERSION_CODES.JELLY_BEAN )
    private void setupGUI( View layout ) {
        // Set other components
        final LinearLayout llTips = (LinearLayout) layout.findViewById( R.id.llTips );
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
        } );
    }

    /**
     * Stores the pip in a temporal variable
     * @param tempPIP The PIP value
     */
    private void setTempPIP( String tempPIP ) {
        this.mTempPIP = tempPIP;
    }

    @Override
    public void execute() {
        mAlertDialog.show();
        clearGUI();
        sbTips.setProgress( 0 );
    }

    @Override
    public void onPrepare() {
        PrefUtils.setSubscribing( mActivity, false );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        mProgressManager.destroyProgressDialog();
        mRequestManager.setListener( (MainActivity) mActivity );

        // Get the response code
        String code = response.getCode();

        switch( responseCode ) {
            case AUTH_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ( (MainActivity) mActivity).payment( mTempPIP, mTempTip );
                } else {
                    String message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
                }

                setTempPIP( null );
                break;
        }
    }
}
