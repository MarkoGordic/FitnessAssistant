package com.example.authentication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.network.NetworkManager;
import com.example.util.authentication.AuthFunctional;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class CreateAccountActivity extends AppCompatActivity {
    private NetworkManager networkManager;

    // sets up listeners for back button and register button
    private void setUpOnClickListeners(){
        // backButton listener - goes back to SignInActivity
        findViewById(R.id.backToSignInButton).setOnClickListener(view -> onBackPressed());

        // registerButton Listener - validates input and checkbox checked and if online creates a new user
        findViewById(R.id.registerButton).setOnClickListener(view -> {
            EditText nameEdit = findViewById(R.id.usernameEditTextForRegister);
            EditText emailEdit = findViewById(R.id.emailEditTextForRegister);
            EditText passEdit = findViewById(R.id.passwordEditTextForRegister);
            EditText cPassEdit = findViewById(R.id.confirmPasswordEditTextForRegister);
            String name = nameEdit.getText().toString();
            String email = emailEdit.getText().toString();
            String password = passEdit.getText().toString();
            String cPassword = cPassEdit.getText().toString();
            if(AuthFunctional.validUsername(getApplicationContext(), nameEdit) && AuthFunctional.validEmail(getApplicationContext(), emailEdit) && AuthFunctional.validPassword(getApplicationContext(), passEdit)){
                if(!password.equals(cPassword)) // check if password and confirmPassword are equal
                    AuthFunctional.bothPasswordsError(getApplicationContext(), passEdit, cPassEdit, getString(R.string.passwords_not_equal));
                else if(!((CheckBox) findViewById(R.id.registerCheckbox)).isChecked())
                    AuthFunctional.checkboxFlash(getApplicationContext(), findViewById(R.id.registerCheckbox));
                else{ // if everything is set, create the user
                    AuthFunctional.startLoading(view, findViewById(R.id.registerBar));
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                        if(!task.isSuccessful()){ // if task fails, network error needs to be checked
                            AuthFunctional.finishLoading(view, findViewById(R.id.registerBar));
                            try{
                                if(task.getException() != null)
                                    throw task.getException();
                            } catch (FirebaseNetworkException e1){ // if it's a network error, the animated notification quickly flashes
                                AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
                            } catch (Exception e2){ // else errors are checked
                                AuthFunctional.emailAlreadyRegistered(getApplicationContext(), emailEdit, email);
                                // this should rarely happen
                                Toast.makeText(getApplicationContext(), getString(R.string.register_unsuccessful), Toast.LENGTH_LONG).show();
                            }
                        } else{
                            // user is created
                            FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
                            if(newUser != null) // probably redundant
                                newUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build()).addOnCompleteListener(task1 -> {
                                    // update user's username
                                    if(!task1.isSuccessful()) // if unsuccessful, check errors
                                        try{ AuthFunctional.finishLoading(view, findViewById(R.id.registerBar));
                                            if(task1.getException() != null)
                                                throw task1.getException();
                                        } catch (FirebaseNetworkException e1){ // if it's a network error, the animated notification quickly flashes
                                            AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
                                        } catch (Exception e2){ // else notify user
                                            Toast.makeText(getApplicationContext(), getString(R.string.account_created_username_update_unsuccessful), Toast.LENGTH_LONG).show();
                                        }
                                    else {
                                        if (AuthFunctional.currentlyOnline){
                                            newUser.reload().addOnFailureListener(e -> Toast.makeText(getApplicationContext(), getString(R.string.register_unsuccessful), Toast.LENGTH_LONG).show());
                                        }
                                        AuthFunctional.finishLoading(view, findViewById(R.id.registerBar));
                                        finish(); // finish after (not) calling reload -> get back to sign in, trigger the listener that will get the user to the home page
                                    }
                                });
                            else // finishing loading here too, just in case (probably redundant)
                                AuthFunctional.finishLoading(view, findViewById(R.id.registerBar));
                        }
                    });
                }
            }
        });

        // TODO Terms of Service and Privacy Policy listeners -> redirecting to scrollViews made for reading with a back button (think about where the user clicks)
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);
        AuthFunctional.setUpPassword(findViewById(R.id.passwordEditTextForRegister));
        AuthFunctional.setUpPassword(findViewById(R.id.confirmPasswordEditTextForRegister));
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