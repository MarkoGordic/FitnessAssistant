package com.example.authentication;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.network.NetworkManager;
import com.example.util.authentication.AuthFunctional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {
    private NetworkManager networkManager;

    // sets up listeners for back button and register button
    private void setUpOnClickListeners(){
        // backButton listener
        findViewById(R.id.backToSignInButton).setOnClickListener(view -> onBackPressed());

        // registerButton Listener
        findViewById(R.id.registerButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline){
                EditText nameEdit = findViewById(R.id.usernameEditForRegister);
                EditText emailEdit = findViewById(R.id.emailEditForRegister);
                EditText passEdit = findViewById(R.id.passwordEditForRegister);
                EditText cPassEdit = findViewById(R.id.confirmPasswordEditForRegister);
                String name = nameEdit.getText().toString();
                String email = emailEdit.getText().toString();
                String password = passEdit.getText().toString();
                String cPassword = cPassEdit.getText().toString();
                // handling errors
                if(TextUtils.isEmpty(name))
                    AuthFunctional.myError(getApplicationContext(), nameEdit, getString(R.string.empty_name));
                else if(TextUtils.isEmpty(email))
                    AuthFunctional.myError(getApplicationContext(), emailEdit, getString(R.string.empty_email));
                else if(TextUtils.isEmpty(password))
                    AuthFunctional.myError(getApplicationContext(), passEdit, getString(R.string.empty_password));
                else if(TextUtils.isEmpty(cPassword))
                    AuthFunctional.myError(getApplicationContext(), cPassEdit, getString(R.string.empty_password));
                else if (passEdit.getText().length() <= 5)
                    AuthFunctional.myError(getApplicationContext(), passEdit, getString(R.string.password_not_enough_characters));
                else if(!passEdit.getText().toString().equals(cPassEdit.getText().toString()))
                    AuthFunctional.bothPasswordsError(getApplicationContext(), passEdit, cPassEdit, getString(R.string.passwords_not_equal));
                else if(AuthFunctional.validUsername(getApplicationContext(), nameEdit) && AuthFunctional.validEmail(getApplicationContext(), emailEdit) && AuthFunctional.validPassword(getApplicationContext(), passEdit)){
                    // after validating all input, check if checkbox is checked :D
                    if(!((CheckBox) findViewById(R.id.registerCheckbox)).isChecked()) {
                        AuthFunctional.checkboxFlash(getApplicationContext(), findViewById(R.id.registerCheckbox));
                    } else{ // if it is, create the user
                        AuthFunctional.startLoading(findViewById(R.id.registerButton), findViewById(R.id.registerBar));
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                            AuthFunctional.finishLoading(findViewById(R.id.registerButton), findViewById(R.id.registerBar));
                            if(!task.isSuccessful()){
                                AuthFunctional.emailAlreadyRegistered(getApplicationContext(), emailEdit, email);
                                // this should rarely happen or not even happen at all
                                Toast.makeText(getApplicationContext(),getString(R.string.register_unsuccessful), Toast.LENGTH_LONG).show();
                            } else{
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                AuthFunctional.updateUserName(user, name);
                                // user created
                                finish();
                            }
                        });
                    }
                }
            } else // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.registerButton), findViewById(R.id.notification_layout_id));
        });

        // TODO Terms of Service and Privacy Policy listeners -> redirecting to scrollViews made for reading with a back button
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting up createAccount UI
        setContentView(R.layout.create_account_screen);
        AuthFunctional.setUpPassword(findViewById(R.id.passwordEditForRegister));
        AuthFunctional.setUpPassword(findViewById(R.id.confirmPasswordEditForRegister));
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());
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
