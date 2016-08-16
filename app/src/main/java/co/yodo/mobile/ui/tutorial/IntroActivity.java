package co.yodo.mobile.ui.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.tutorial.slides.TutoSlide;

/**
 * Created by hei on 12/08/16.
 * Handles the tutorial
 */
public class IntroActivity extends AppIntro {
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addSlide( TutoSlide.newInstance( R.layout.intro1 ) );
        addSlide( TutoSlide.newInstance( R.layout.intro2 ) );
        addSlide( TutoSlide.newInstance( R.layout.intro3 ) );
        addSlide( TutoSlide.newInstance( R.layout.intro4 ) );

        setZoomAnimation();
    }

    @Override
    public void onSkipPressed( Fragment currentFragment ) {
        finish();
    }

    @Override
    public void onDonePressed( Fragment currentFragment ) {
        finish();
    }

    public void getStarted( View v ){
        finish();
    }
}
