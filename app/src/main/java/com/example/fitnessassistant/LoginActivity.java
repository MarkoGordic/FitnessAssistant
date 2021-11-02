package com.example.fitnessassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private void loadingAnimation(){
        TextView appName = findViewById(R.id.FitnessAssistant);
        Animation loadAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fitnessassistant_loading);

        loadAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        appName.startAnimation(loadAnim);
    }

    // TODO Handle email and password null
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        if(prefs.contains("email") && prefs.contains("password")){
            // TODO Fix animation flickering
            setContentView(R.layout.main_screen);
            loadingAnimation();
            String emailText = prefs.getString("email", "");
            String passwordText = prefs.getString("password", "");
            loginUser(emailText, passwordText);
        } else{
            setContentView(R.layout.login_screen);
            EditText email = findViewById(R.id.edtTxtEmail);
            EditText password = findViewById(R.id.edtTxtPassword);
            Button login = findViewById(R.id.signUpButton);

            login.setOnClickListener((View v)->{
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                loginUser(emailText, passwordText);
            });
        }
    }

    private void loginUser(String email, String password){
        Task<AuthResult> task = auth.signInWithEmailAndPassword(email, password);
        task.addOnSuccessListener(authResult -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("email", email);
            editor.putString("password", password);
            editor.apply();
            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
            finish();
        });
        task.addOnFailureListener(e -> {
            // TODO Make credentials red (don't clear them completely)
            Toast.makeText(LoginActivity.this, "Credentials Incorrect!", Toast.LENGTH_SHORT).show();
        });
    }
}
