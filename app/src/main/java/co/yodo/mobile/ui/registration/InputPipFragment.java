package co.yodo.mobile.ui.registration;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.utils.PipUtils;

/**
 * Created by hei on 04/03/17.
 * Registration of the PIP
 */
public class InputPipFragment extends Fragment {
    /** Extras for the fragment */
    private static final String EXTRA_IS_REGISTRATION = "EXTRA_IS_REGISTRATION";

    /** Application context */
    @Inject
    Context context;

    /** GUI Controllers */
    @BindView(R.id.tietPip)
    TextInputEditText etPip;

    @BindView(R.id.tietConfirmPip)
    TextInputEditText etPipConfirm;

    @BindView(R.id.llCurrency)
    LinearLayout llCurrency;

    @BindView(R.id.tvCurrency)
    TextView tvCurrency;

    @BindView(R.id.tvCurrencyError)
    TextView tvCurrencyError;

    /**
     * Creates a new fragment with parameters
     * @param isRegistration If the layout is for registering a new user
     * @return The fragment
     */
    public static InputPipFragment newInstance(boolean isRegistration) {
        InputPipFragment fragment = new InputPipFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_REGISTRATION, isRegistration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_input_pip, container, false);

        // Injection
        ButterKnife.bind(this, view);
        YodoApplication.getComponent().inject(this);
        updateData();

        return view;
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        if (getArguments() != null) {
            final boolean isRegistration = getArguments().getBoolean(EXTRA_IS_REGISTRATION, false);
            if (isRegistration) {
                setupCurrencies();
            }
        }
    }

    private void setupCurrencies() {
        llCurrency.setVisibility(View.VISIBLE);
        tvCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CurrencyPicker picker = CurrencyPicker.newInstance(getString(R.string.text_currency_title));
                picker.setListener(new CurrencyListAdapter.CurrencyPickerListener() {
                    @Override
                    public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
                        picker.dismiss();
                        tvCurrency.setText(code);
                    }
                });
                picker.show(getActivity().getSupportFragmentManager(), "CURRENCY_PICKER");
            }
        });
    }

    /**
     * Validates the PIP
     * - Shouldn't be empty
     * - Minimum of 4 characters
     * - Match with the confirmation pip
     * @return The pip if the validation was a success
     */
    public String validatePIP() {
        String pip = null;
        // Validate the pip and confirmation
        if (PipUtils.validate(context, etPip, etPipConfirm)) {
            pip = etPip.getText().toString();
        }
        return pip;
    }

    /**
     * Validates if the currency is set, otherwise returns null and shows
     * and error message
     * @return The currency code
     */
    public String validateCurrency() {
        // Validate currency
        final String currency = tvCurrency.getText().toString();
        if (currency.equals(getString(R.string.text_currency_hint))) {
            tvCurrencyError.setText(R.string.error_currency);
            return null;
        }
        return currency;
    }
}
