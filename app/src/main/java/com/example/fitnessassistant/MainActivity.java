package com.example.fitnessassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private void openingAnimation(){
        TextView appName = findViewById(R.id.FitnessAssistant);
        Animation openAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fitnessassistant_opening);

        openAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        appName.startAnimation(openAnim);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        openingAnimation();
    }

}
