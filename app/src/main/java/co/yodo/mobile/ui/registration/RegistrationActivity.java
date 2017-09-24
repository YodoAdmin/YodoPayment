package co.yodo.mobile.ui.registration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.helper.EulaHelper;
import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.helper.ProgressDialogHelper;
import co.yodo.mobile.model.dtos.FirebaseAuthError;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.utils.ErrorUtils;
import timber.log.Timber;

public class RegistrationActivity extends BaseActivity {
    /** Phone verification states */
    private enum VerificationState {
        VERIFICATION_NOT_STARTED, VERIFICATION_STARTED, VERIFIED
    }

    /** The application context */
    @Inject
    Context context;

    /** Firebase authentication object */
    @Inject
    FirebaseAuth auth;

    @Inject
    PhoneAuthProvider phoneAuth;

    /** Timeout for the validation */
    private static final long TIMEOUT_RETRIEVAL = 60;

    /** Phone provider registration elements */
    private String phoneNumber;
    private String verificationId;
    private VerificationState verificationState;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    /** Dialog for the confirmation code */
    private SubmitCodeDialog submitCodeDialog;

    /** GUI Controllers */
    @BindView(R.id.acbRegister)
    AppCompatButton registerButton;

    /** Fragment handler */
    private FragmentManager fragmentManager;

    /** Fragment tags */
    private static final String TAG_REG_PNE = "TAG_REG_PNE";
    private static final String TAG_REG_PIP = "TAG_REG_PIP";
    private static final String TAG_REG_BIO = "TAG_REG_BIO";
    private String currentFragment;

    /** State keys */
    private static final String KEY_VERIFICATION_PHONE = "KEY_VERIFICATION_PHONE";
    private static final String KEY_STATE = "KEY_STATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setupGUI(savedInstanceState);
        updateData(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (verificationState.equals(VerificationState.VERIFICATION_STARTED)) {
            sendCode(phoneNumber, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_STATE, verificationState);
        outState.putString(KEY_VERIFICATION_PHONE, phoneNumber);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        ProgressDialogHelper.dismiss();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupGUI(final Bundle savedInstanceState) {
        super.setupGUI(savedInstanceState);
        // Injection
        YodoApplication.getComponent().inject(this);

        // Setup the fragment manager
        fragmentManager = getSupportFragmentManager();

        // Show the terms to the user
        EulaHelper.show(this, new EulaHelper.EulaCallback() {
            @Override
            public void onEulaAgreedTo() {
                registerButton.setVisibility(View.VISIBLE);

                // Check that the activity is using the layout version with
                // the fragment_container FrameLayout
                if (findViewById(R.id.fragment_container) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }

                    // Create a new Fragment to be placed in the activity layout
                    Fragment fragment;
                    if (PreferencesHelper.getPhoneNumber() == null) {
                        fragment = new InputPhoneFragment();
                        currentFragment = TAG_REG_PNE;
                    }
                    else if (PreferencesHelper.getAuthNumber() == null) {
                        fragment = new InputPipFragment();
                        currentFragment = TAG_REG_PIP;
                    }
                    else {
                        fragment = new InputBiometricFragment();
                        currentFragment = TAG_REG_BIO;
                    }

                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, fragment, currentFragment)
                            .commit();
                }
            }
        } );

        // Initialize callbacks
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Timber.d("onVerificationCompleted:" + credential);
                verificationState = VerificationState.VERIFIED;

                if (TextUtils.isEmpty(credential.getSmsCode())) {
                    signIn(credential);
                } else {
                    //Show Fragment if it is not already visible
                    showSubmitCodeDialog();

                    ProgressDialogHelper.create(RegistrationActivity.this, R.string.text_phone_confirmation_retrieving);
                    if (submitCodeDialog != null) {
                        submitCodeDialog.setConfirmationCode(String.valueOf(credential.getSmsCode()));
                    }
                    signIn(credential);
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                ProgressDialogHelper.dismiss();
                ErrorUtils.handleFirebaseError(RegistrationActivity.this, e);
            }

            @Override
            public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken token) {
                Timber.d("onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                verificationId = id;
                resendToken = token;

                ProgressDialogHelper.dismiss();
                showSubmitCodeDialog();
            }
        };
    }

    /**
     * Updates the data of the activity
     * @param savedInstanceState The saved state
     */
    private void updateData(Bundle savedInstanceState) {
        verificationState = VerificationState.VERIFICATION_NOT_STARTED;

        phoneNumber = PreferencesHelper.getPhoneNumber();
        if (phoneNumber != null) {
            verificationState = VerificationState.VERIFIED;
        }
        else if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            phoneNumber = savedInstanceState.getString(KEY_VERIFICATION_PHONE);
            if (savedInstanceState.getSerializable(KEY_STATE) != null) {
                verificationState = (VerificationState) savedInstanceState.getSerializable(KEY_STATE);
            }
        }
    }

    /**
     * Creates a new instance of the registration activity
     * @param context The application context
     */
    public static void newInstance(Context context) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        context.startActivity(intent);
    }

    /**
     * Validates the phone number
     * @param phoneNumber The phone number
     */
    public void verifyPhoneNumber(String phoneNumber, boolean forceResend) {
        sendCode(phoneNumber, forceResend);
        if (forceResend) {
            ProgressDialogHelper.create(this, R.string.text_phone_resending);
        } else {
            ProgressDialogHelper.create(this, R.string.text_phone_verifying);
        }
    }

    public void submitConfirmationCode(String confirmationCode) {
        ProgressDialogHelper.create(this, R.string.text_phone_verifying);
        signIn(PhoneAuthProvider.getCredential(verificationId, confirmationCode));
    }

    /**
     * The next button for the registration, handles several actions depending
     * in the current fragment
     * @param view, The view, not used
     */
    public void next(View view) {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        switch (currentFragment.getTag()) {
            case TAG_REG_PNE:
                if (((InputPhoneFragment) currentFragment).validatePhoneNumber() != null) {
                    InputPipFragment pipFragment = new InputPipFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, pipFragment, TAG_REG_PIP)
                            .commit();
                }
                break;

            case TAG_REG_PIP:
                final String pip = ((InputPipFragment) currentFragment).validatePIP();
                if (pip != null) {
                    InputBiometricFragment bioFragment = InputBiometricFragment.newInstance(
                            phoneNumber,
                            pip
                    );
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, bioFragment, TAG_REG_BIO)
                            .addToBackStack(null)
                            .commit();
                }
                break;

            default:
                InputBiometricFragment bioFragment = ((InputBiometricFragment) currentFragment);
                bioFragment.registerUser();
                break;
        }
    }

    /**
     * Sends a code for validation
     * @param phoneNumber The phone number that the user provided
     * @param forceResend If it is a retry
     */
    private void sendCode(String phoneNumber, boolean forceResend) {
        this.phoneNumber = phoneNumber;
        verificationState = VerificationState.VERIFICATION_STARTED;

        phoneAuth.verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                TIMEOUT_RETRIEVAL,  // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                callbacks,          // OnVerificationStateChangedCallbacks
                forceResend ? resendToken : null);
    }

    /**
     * Validates the user credentials
     * @param credential The credentials generated from the phone
     */
    private void signIn(@NonNull PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(final AuthResult authResult) {
                        PreferencesHelper.setPhoneNumber(authResult.getUser().getPhoneNumber());
                        ProgressDialogHelper.dismiss();
                        submitCodeDialog.dismiss();
                        verificationState = VerificationState.VERIFIED;

                        InputPhoneFragment currentFragment = (InputPhoneFragment) fragmentManager.findFragmentById(R.id.fragment_container);
                        currentFragment.setValidatedUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ProgressDialogHelper.dismiss();

                        //incorrect confirmation code
                        View root = findViewById(android.R.id.content);
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            FirebaseAuthError error = FirebaseAuthError.fromException(
                                    (FirebaseAuthInvalidCredentialsException) e);

                            switch (error) {
                                case ERROR_INVALID_VERIFICATION_CODE:
                                    Snackbar.make(root, R.string.error_phone_confirmation, Snackbar.LENGTH_LONG).show();
                                    break;

                                case ERROR_SESSION_EXPIRED:
                                    Snackbar.make(root, R.string.error_phone_session_expired, Snackbar.LENGTH_LONG).show();
                                    break;

                                default:
                                    Timber.w(error.getDescription(), e);
                                    Snackbar.make(root, error.getDescription(), Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(root, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Shows a dialog to input the validation code
     */
    private void showSubmitCodeDialog() {
        if (submitCodeDialog == null || !submitCodeDialog.isShowing()) {
            submitCodeDialog = new SubmitCodeDialog(this, phoneNumber);
        }
    }
}
