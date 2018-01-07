package co.yodo.mobile.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by hei on 08/08/17.
 * Base fragment that implements some basic stuff
 */
public abstract class BaseFragment extends Fragment {
    /** Unbinder */
    private Unbinder unbinder;

    protected void setupGUI(View view) {
        // Injection
        unbinder = ButterKnife.bind( this, view );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
