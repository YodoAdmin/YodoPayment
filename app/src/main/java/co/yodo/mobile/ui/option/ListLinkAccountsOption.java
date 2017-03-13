package co.yodo.mobile.ui.option;

import android.view.View;

import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the DeLink Account Option of the MainActivity
 */
public class ListLinkAccountsOption extends IRequestOption /*implements ApiClient.RequestsCallback*/ {
    /** Temporal */
    private String mTempPIP;

    /** Response codes for the server requests */
    private static final int QUERY_LNK_ACC_REQ = 0x04;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public ListLinkAccountsOption( MainActivity activity ) {
        super( activity );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                /*try {
                    if( mPipValidator.validate( etInput ) ) {
                        alertDialog.dismiss();

                        // Set a temporary PIP and Code
                        final String pip = TOTPUtils.defaultOTP( etInput.getText().toString() );
                        setTempPIP( pip );

                        progressManager.newInstance( activity );
                        //requestManager.setListener( DeLinkAccountOption.this );
                        requestManager.invoke( new QueryRequest(
                                QUERY_LNK_ACC_REQ,
                                hardwareToken,
                                mTempPIP,
                                QueryRequest.Record.LINKED_ACCOUNTS
                        ) );
                    }
                } catch( NoSuchFieldException e ) {
                    e.printStackTrace();
                }*/
            }
        };

        alertDialog = AlertDialogHelper.create(
                this.activity,
                layout,
                buildOnClick( okClick )
        );
    }

    private void setTempPIP( String tempPIP ) {
        this.mTempPIP = tempPIP;
    }

    @Override
    public void execute() {
        alertDialog.show();
        clearGUI();
    }

    /*@Override
    public void onPrepare() {
        PrefUtils.setSubscribing( activity, false );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        progressManager.destroy();
        requestManager.setListener( ( (MainActivity) activity) );

        // Get the response code
        String code = response.getCode();
        String message;

        switch( responseCode ) {
            case QUERY_LNK_ACC_REQ:
                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        String from = response.getParams().getLinkedFrom();
                        String to = response.getParams().getLinkedTo();

                        Intent i = new Intent( activity, DeLinkActivity.class );
                        i.putExtra( Intents.LINKED_ACC_TO, to );
                        i.putExtra( Intents.LINKED_ACC_FROM, from );
                        i.putExtra( Intents.LINKED_PIP, mTempPIP );
                        activity.startActivity( i );
                        break;

                    // We don't have links
                    case ServerResponse.ERROR_FAILED:
                        message = activity.getString( R.string.error_message_no_links );
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;

                    // If it is something else, show the error
                    default:
                        message = response.getMessage();
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }

                setTempPIP( null );
                break;
        }
    }*/
}
