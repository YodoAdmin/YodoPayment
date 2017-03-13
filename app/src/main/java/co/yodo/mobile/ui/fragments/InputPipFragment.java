package co.yodo.mobile.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    /** Application context */
    @Inject
    Context context;

    /** GUI Controllers */
    @BindView( R.id.text_pip )
    TextInputEditText etPip;

    @BindView( R.id.text_confirm_pip )
    TextInputEditText etPipConfirm;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_input_pip, container, false );

        // Injection
        ButterKnife.bind( this, view );
        YodoApplication.getComponent().inject( this );

        return view;
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
        if( PipUtils.validate( context, etPip, etPipConfirm ) ) {
            pip = etPip.getText().toString();
        }
        return pip;
    }
}
