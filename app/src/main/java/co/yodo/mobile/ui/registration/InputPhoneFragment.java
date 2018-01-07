package co.yodo.mobile.ui.registration;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import co.yodo.mobile.R;
import co.yodo.mobile.helper.ImeHelper;
import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.model.dtos.CountryInfo;
import co.yodo.mobile.ui.fragments.BaseFragment;
import co.yodo.mobile.ui.phone.CountryListSpinner;
import co.yodo.mobile.utils.PhoneNumberUtils;

/**
 * Created by hei on 07/08/17.
 * Gets the user phone number
 */
public class InputPhoneFragment extends BaseFragment implements View.OnClickListener {
    /** Application context */
    private Context context;

    /** Registration activity that handles the validations */
    private RegistrationActivity verifier;

    /** UI Controllers */
    @BindView(R.id.clsCountries) CountryListSpinner clsCountries;
    @BindView(R.id.etPhoneNumber) EditText etPhoneNumber;
    @BindView(R.id.tvPhoneNumberError) TextView tvPhoneNumberError;
    @BindView(R.id.tvPhoneTerms) TextView tvPhoneTerms;
    @BindView(R.id.bSendCode) Button bSendCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_input_phone, container, false);

        setupGUI(view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set listener
        if (!(getActivity() instanceof RegistrationActivity)) {
            throw new IllegalStateException("Activity must be RegistrationActivity");
        }
        verifier = (RegistrationActivity) getActivity();
    }

    @Override
    protected void setupGUI(View view) {
        super.setupGUI(view);

        ImeHelper.setImeOnDoneListener(etPhoneNumber, new ImeHelper.DonePressedListener() {
            @Override
            public void onDonePressed() {
                onNext();
            }
        });

        setUpCountrySpinner();
        setupSendCodeButton();
        setupTerms();
    }

    @Override
    public void onClick(View view) {
        onNext();
    }

    void setValidatedUI() {
        bSendCode.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
        bSendCode.setTextColor(ContextCompat.getColor(context, R.color.colorTextWhite));
        bSendCode.setText(R.string.text_phone_validated);
        bSendCode.setEnabled(false);
        etPhoneNumber.setEnabled(false);
        clsCountries.setEnabled(false);
    }

    String validatePhoneNumber() {
        final String phoneNumber = PreferencesHelper.getPhoneNumber();
        if (phoneNumber == null) {
            showError(getString(R.string.error_phone));
        }

        return phoneNumber;
    }

    /** Next step for the validation */
    private void onNext() {
        final String phoneNumber = getPseudoValidPhoneNumber();
        if (phoneNumber == null) {
            tvPhoneNumberError.setText(R.string.error_phone_invalid);
        } else {
            showError(null);
            verifier.verifyPhoneNumber(phoneNumber, false);
        }
    }

    /**
     * Shows an error for the password
     * @param e The error message
     */
    public void showError(String e) {
        tvPhoneNumberError.setText(e);
    }

    /**
     * Clear error when spinner is clicked on
     */
    private void setUpCountrySpinner() {
        clsCountries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPhoneNumberError.setText("");
            }
        });
    }

    /**
     * Setups the send code button
     */
    private void setupSendCodeButton() {
        bSendCode.setOnClickListener(this);
    }

    /**
     * Sets the texts for the validation phone fragment
     */
    private void setupTerms() {
        final String verifyPhoneButtonText = getString(R.string.text_phone_verify);
        final String terms = getString(R.string.text_phone_terms, verifyPhoneButtonText);
        tvPhoneTerms.setText(terms);
    }

    @Nullable
    private String getPseudoValidPhoneNumber() {
        final CountryInfo countryInfo = (CountryInfo) clsCountries.getTag();
        final String everythingElse = etPhoneNumber.getText().toString();

        if (TextUtils.isEmpty(everythingElse)) {
            return null;
        }

        return PhoneNumberUtils.format(everythingElse, countryInfo);
    }
}
