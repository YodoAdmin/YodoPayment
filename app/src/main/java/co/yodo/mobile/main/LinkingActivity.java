package co.yodo.mobile.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.component.ClearEditText;
import co.yodo.mobile.component.ToastMaster;
import co.yodo.mobile.component.YodoHandler;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.net.YodoRequest;

public class LinkingActivity extends ActionBarActivity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Hardware Identifier */
    private String hardwareToken;

    /*!< Link Account */
    private String account_type;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( LinkingActivity.this );
        setContentView(R.layout.activity_linking);

        setupGUI();
        updateData();

        if( savedInstanceState != null && savedInstanceState.getBoolean( AppConfig.IS_SHOWING ) ) {
            YodoRequest.getInstance().createProgressDialog(
                    LinkingActivity.this ,
                    YodoRequest.ProgressDialogType.NORMAL
            );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(
                AppConfig.IS_SHOWING,
                YodoRequest.getInstance().progressDialogShowing()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YodoRequest.getInstance().destroyProgressDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupGUI() {
        // get the context
        ac = LinkingActivity.this;
        // Handler
        handlerMessages = new YodoHandler( LinkingActivity.this );

        // Only used at creation
        Toolbar actionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar( actionBarToolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
        hardwareToken = AppUtils.getHardwareToken( ac );

        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    public void generateLinkCodeClick(final View v) {
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item1) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( LinkingActivity.this );

                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                    ToastMaster.makeText( LinkingActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                } else {
                    YodoRequest.getInstance().createProgressDialog(
                            LinkingActivity.this,
                            YodoRequest.ProgressDialogType.NORMAL
                    );

                    YodoRequest.getInstance().requestLinkingCode(
                            LinkingActivity.this,
                            hardwareToken, pip
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
    }

    public void linkOptionsClick(final View v) {
        final EditText inputBox = new ClearEditText( ac );
        String[] options = getResources().getStringArray( R.array.link_options_array );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int item) {
                switch( item ) {
                    case 0:
                        String title = getString( R.string.input_linking_code );

                        DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String linkingCode = inputBox.getText().toString();
                                account_type = v.getContentDescription().toString();

                                YodoRequest.getInstance().createProgressDialog(
                                        LinkingActivity.this,
                                        YodoRequest.ProgressDialogType.NORMAL
                                );

                                YodoRequest.getInstance().requestLinkAccount(
                                        LinkingActivity.this,
                                        hardwareToken, linkingCode
                                );
                            }
                        };

                        AlertDialogHelper.showAlertDialog(
                                ac,
                                title,
                                null, getString( R.string.show_linking_code ),
                                inputBox,
                                okClick
                        );

                        break;

                    case 1:
                        title = getString( R.string.input_pip );

                        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item1) {
                                String pip = inputBox.getText().toString();
                                AppUtils.hideSoftKeyboard( LinkingActivity.this );

                                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                                    ToastMaster.makeText( LinkingActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                                } else {
                                    YodoRequest.getInstance().createProgressDialog(
                                            LinkingActivity.this,
                                            YodoRequest.ProgressDialogType.NORMAL
                                    );

                                    YodoRequest.getInstance().requestLinkedAccounts(
                                            LinkingActivity.this,
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

                        break;
                }
            }
        };

        AlertDialogHelper.showAlertDialog( ac, getString( R.string.linking_menu ), options, onClick );
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        YodoRequest.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case QUERY_LNK_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String linking_code = response.getParam( ServerResponse.LINKING_CODE );

                    Dialog dialog = new Dialog( LinkingActivity.this );
                    dialog.getWindow();
                    dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
                    dialog.setContentView( R.layout.dialog_linking_code );

                    final TextView codeText   = (TextView) dialog.findViewById(R.id.codeText);
                    ImageView codeImage = (ImageView) dialog.findViewById(R.id.copyCodeImage);
                    codeText.setText(linking_code);

                    codeImage.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppUtils.copyCode( ac, codeText.getText().toString() );
                            ToastMaster.makeText( LinkingActivity.this, R.string.copied_text, Toast.LENGTH_SHORT ).show();
                        }
                    });

                    dialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });

                    dialog.show();
                } else {
                    message = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }
                break;

            case LINK_ACC_REQUEST:
                code = response.getCode();
                message = response.getMessage();
                AppUtils.sendMessage( handlerMessages, code, message );

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.saveLinkedAccount( ac, account_type );
                    account_type = null;
                }
                break;

            case QUERY_LNK_ACC_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String to   = response.getParam( ServerResponse.TO );
                    String from = response.getParam( ServerResponse.FROM );

                    Intent i = new Intent( LinkingActivity.this, DeLinkActivity.class );
                    i.putExtra( Intents.LINKED_ACC_TO, to );
                    i.putExtra( Intents.LINKED_ACC_FROM, from );
                    startActivity( i );
                } else {
                    message = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }
                break;
        }
    }
}
