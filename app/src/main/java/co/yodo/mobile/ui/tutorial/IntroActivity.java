package co.yodo.mobile.ui.tutorial;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.tutorial.slides.TutorialSlide;

/**
 * Created by hei on 12/08/16.
 * Handles the tutorial
 */
public class IntroActivity extends AppIntro {
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Set slides
        addSlide( TutorialSlide.newInstance( R.layout.fragment_intro_1 ) );
        addSlide( TutorialSlide.newInstance( R.layout.fragment_intro_2 ) );
        addSlide( TutorialSlide.newInstance( R.layout.fragment_intro_3 ) );
        addSlide( TutorialSlide.newInstance( R.layout.fragment_intro_4 ) );

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

    public void getStarted( View v ) {
        finish();
    }
}