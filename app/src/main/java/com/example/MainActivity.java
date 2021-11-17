package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.authentication.R;
import com.example.authentication.SignInActivity;

public class MainActivity extends AppCompatActivity {

    // used at the start of the app
    private void openingAnimation(){
        setContentView(R.layout.loading_screen);
        Animation openAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.authentication_opening);
        openAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
            }
        });
        findViewById(R.id.appLogo).startAnimation(openAnim);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openingAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}