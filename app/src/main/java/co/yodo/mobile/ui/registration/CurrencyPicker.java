package co.yodo.mobile.ui.registration;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import co.yodo.mobile.R;

/**
 * Created by hei on 27/11/17.
 * Shows a list of currencies with flags
 */
public class CurrencyPicker extends DialogFragment {
    /** Extras */
    private static final String EXTRA_TITLE = "EXTRA_TITLE";

    /** UI controllers */
    private CurrencyListAdapter adapter;
    private List<ExtendedCurrency> currenciesList = new ArrayList<>();
    private List<ExtendedCurrency> selectedCurrenciesList = new ArrayList<>();
    private CurrencyListAdapter.CurrencyPickerListener listener;

    /**
     * To support show as dialog
     */
    public static CurrencyPicker newInstance(String dialogTitle) {
        CurrencyPicker picker = new CurrencyPicker();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, dialogTitle);
        picker.setArguments(bundle);
        return picker;
    }

    public CurrencyPicker() {
        setCurrenciesList(ExtendedCurrency.getAllCurrencies());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_currency_picker, container, false);
        Bundle args = getArguments();
        if (args != null && getDialog() != null) {
            String dialogTitle = args.getString(EXTRA_TITLE);
            getDialog().setTitle(dialogTitle);

            int width = getResources().getDimensionPixelSize(R.dimen.cp_dialog_width);
            int height = getResources().getDimensionPixelSize(R.dimen.cp_dialog_height);
            Window window = getDialog().getWindow();
            if (window != null) {
                window.setLayout(width, height);
            }
        }
        /* UI components */
        EditText searchEditText = view.findViewById(R.id.currency_code_picker_search);
        ListView currencyListView = view.findViewById(R.id.currency_code_picker_listview);

        selectedCurrenciesList = new ArrayList<>(currenciesList.size());
        selectedCurrenciesList.addAll(currenciesList);

        adapter = new CurrencyListAdapter(getActivity(), selectedCurrenciesList);
        currencyListView.setAdapter(adapter);

        currencyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    ExtendedCurrency currency = selectedCurrenciesList.get(position);
                    listener.onSelectCurrency(currency.getName(), currency.getCode(), currency.getSymbol(),
                            currency.getFlag());
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        return view;
    }

    @Override
    public void dismiss() {
        if (getDialog() != null) {
            super.dismiss();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public void setListener(CurrencyListAdapter.CurrencyPickerListener listener) {
        this.listener = listener;
    }

    @SuppressLint("DefaultLocale")
    private void search(String text) {
        selectedCurrenciesList.clear();
        for (ExtendedCurrency currency : currenciesList) {
            if (currency.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
                selectedCurrenciesList.add(currency);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void setCurrenciesList(List<ExtendedCurrency> newCurrencies) {
        this.currenciesList.clear();
        this.currenciesList.addAll(newCurrencies);
    }

    public void setCurrenciesList(Set<String> savedCurrencies) {
        this.currenciesList.clear();
        for(String code : savedCurrencies){
            this.currenciesList.add(ExtendedCurrency.getCurrencyByISO(code));
        }
    }
}
