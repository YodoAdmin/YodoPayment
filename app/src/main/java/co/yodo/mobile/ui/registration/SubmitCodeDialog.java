package co.yodo.mobile.ui.registration;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.ImeHelper;
import co.yodo.mobile.ui.components.SpacedEditText;
import co.yodo.mobile.utils.GuiUtils;

/**
 * Created by hei on 21/08/17.
 * Dialog to submit the confirmation code
 */
class SubmitCodeDialog {
    /** Registration activity */
    private RegistrationActivity activity;

    /** Dialog */
    private AlertDialog dialog;

    /** UI Controllers */
    private SpacedEditText etConfirmationCode;
    private TextView tvPhoneNumber;
    private Button bSubmitConfirmation;
    private TextView tvCountDown;
    private TextView tvResendCode;

    /** Time to resend code */
    private final String resendText;
    private long millisUntilFinished;
    private static final long RESEND_WAIT_MILLIS = 15000;
    private CustomCountDownTimer countdownTimer;

    /**
     * Creates the dialog to confirm the number
     * @param activity The activity that is creating the dialog
     * @param phoneNumber The entered phone number
     */
    SubmitCodeDialog(RegistrationActivity activity, String phoneNumber) {
        // Get context and resources
        this.activity = activity;
        Context context = activity.getApplicationContext();
        resendText = context.getString(R.string.text_phone_confirmation_resend_time);

        // Setup layout
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_with_phone_confirmation, new LinearLayout(activity), false);

        // Setup controllers
        tvPhoneNumber = (TextView) view.findViewById(R.id.tvPhoneNumber);
        etConfirmationCode = (SpacedEditText) view.findViewById(R.id.setConfirmationCode);
        tvCountDown = (TextView) view.findViewById(R.id.tvTicker);
        tvResendCode = (TextView) view.findViewById(R.id.tvResendCode);

        dialog = AlertDialogHelper.show(activity, view, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        bSubmitConfirmation = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        setupConfirmationCodeEditText();
        setupEditPhoneNumberTextView(phoneNumber);
        setupCountDown(RESEND_WAIT_MILLIS);
        setupSubmitConfirmationCodeButton();
        setupResendConfirmationCodeTextView(phoneNumber);
    }

    /**
     * Returns if the dialog if being showed or not
     * @return a boolean
     */
    boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    /**
     * Sets the confirmation code
     * @param code The confirmation code
     */
    void setConfirmationCode(String code) {
        etConfirmationCode.setText(code);
    }

    void dismiss() {
        if (isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void setupConfirmationCodeEditText() {
        etConfirmationCode.setText("------");
        BucketedTextChangeListener listener = createBucketedTextChangeListener();
        etConfirmationCode.addTextChangedListener(listener);
        ImeHelper.setImeOnDoneListener(etConfirmationCode, new ImeHelper.DonePressedListener() {
                    @Override
                    public void onDonePressed() {
                        if (bSubmitConfirmation.isEnabled()) {
                            submitConfirmationCode();
                        }
                    }
                });
    }

    private void setupEditPhoneNumberTextView(@Nullable String phoneNumber) {
        tvPhoneNumber.setText(TextUtils.isEmpty(phoneNumber) ? "" : phoneNumber);
    }

    private void setupCountDown(long startTimeMillis) {
        setTimer(startTimeMillis / 1000);
        countdownTimer = createCountDownTimer(tvCountDown, tvResendCode, this, startTimeMillis);
        startTimer();
    }

    private void setupSubmitConfirmationCodeButton() {
        bSubmitConfirmation.setEnabled(false);
        bSubmitConfirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitConfirmationCode();
            }
        });
    }

    private void setupResendConfirmationCodeTextView(final String phoneNumber) {
        tvResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.verifyPhoneNumber(phoneNumber, true);
                tvResendCode.setVisibility(View.GONE);
                tvCountDown.setVisibility(View.VISIBLE);
                tvCountDown.setText(String.format(resendText, RESEND_WAIT_MILLIS / 1000));
                countdownTimer.renew();
            }
        });
    }

    private void submitConfirmationCode() {
        GuiUtils.hideSoftKeyboard(dialog.getCurrentFocus());
        activity.submitConfirmationCode(etConfirmationCode.getUnspacedText().toString());
    }

    private BucketedTextChangeListener createBucketedTextChangeListener() {
        return new BucketedTextChangeListener(
                etConfirmationCode,
                6,
                "-",
                createBucketOnEditCallback(bSubmitConfirmation)
        );
    }

    private void setTimer(long millisUntilFinished) {
        tvCountDown.setText(String.format(resendText, timeRoundedToSeconds(millisUntilFinished)));
    }

    private void startTimer() {
        if (countdownTimer != null) {
            countdownTimer.start();
        }
    }

    private int timeRoundedToSeconds(double millis) {
        return (int) Math.ceil(millis / 1000);
    }

    /**
     * Creates the countdown timer to resent a code
     * @param timerText The timer TextView to modify
     * @param resendCode The resend code TextView
     * @param dialog The class with the dialog
     * @param startTimeMillis The current time
     * @return The timer
     */
    private CustomCountDownTimer createCountDownTimer(final TextView timerText, final TextView resendCode, final SubmitCodeDialog dialog, final long startTimeMillis) {
        return new CustomCountDownTimer(startTimeMillis, 500) {
            SubmitCodeDialog submitConfirmationCodeDialog = dialog;

            public void onTick(long millis) {
                millisUntilFinished = millis;
                submitConfirmationCodeDialog.setTimer(millisUntilFinished);
            }

            public void onFinish() {
                timerText.setText("");
                timerText.setVisibility(View.GONE);
                resendCode.setVisibility(View.VISIBLE);
            }
        };
    }

    /**
     * Handles the different states of the code
     * @param button The ok button that will be enabled
     * @return The listener
     */
    private BucketedTextChangeListener.ContentChangeCallback createBucketOnEditCallback(final Button button) {
        return new BucketedTextChangeListener.ContentChangeCallback() {
            @Override
            public void whileComplete() {
                button.setEnabled(true);
            }

            @Override
            public void whileIncomplete() {
                button.setEnabled(false);
            }
        };
    }
}
