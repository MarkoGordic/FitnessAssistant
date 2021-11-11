package com.example.authentication;

// TODO Handle wrong email input and password input on creating an account (email doesn't need handling, only send the verification email)

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.network.NetworkManager;
import com.example.util.authentication.AuthFunctional;

public class CreateAccountActivity extends AppCompatActivity {
    private NetworkManager networkManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_screen);
        networkManager = new NetworkManager(getApplication());
        // backButton listener
        findViewById(R.id.backToSignInButton).setOnClickListener(view -> onBackPressed());
        // registerButton Listener
        findViewById(R.id.registerButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline){
                Toast.makeText(this,getString(R.string.empty_email), Toast.LENGTH_SHORT).show();
                // TODO handle onClick for creating an account
                //  also think about Terms of Service and Privacy Policy
            } else
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.registerButton), findViewById(R.id.notification_layout_id));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.createAccountScreen));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
    }
}
