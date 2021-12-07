package com.example.fitnessassistant.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.authentication.SignInActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilePageFragment extends Fragment {
    private NetworkManager networkManager;
    private FirebaseAuth.AuthStateListener authListener;

    // called in onResume -> reloading user and displaying user info
    private void displayCurrentUser(){
        // setting up current user - this is in onResume in case anything gets changed after onPause()
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            // try to reload the user (update) to get the latest data
            currentUser.reload().addOnCompleteListener(task -> {
                if(task.isSuccessful()) // we're displaying the user's profile (reloaded)
                    AuthFunctional.dispUser(getActivity(), currentUser);
                else
                    try{
                        if(task.getException() != null)
                            throw task.getException();
                    } catch (FirebaseNetworkException e1){ // if it fails and it's a network error, the animated notification quickly flashes
                        AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                        // we're displaying the user's profile (last saved)
                        AuthFunctional.dispUser(getActivity(), currentUser);
                    } catch(Exception e2){ // if it fails and we're online(user deleted, disabled or credentials no longer valid) -> return to sign in
                        startActivity(new Intent(getActivity(), SignInActivity.class));
                        requireActivity().finish();
                    }
            });
        }
    }

    // setting up oCListeners
    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClickListeners(View view) {
        // changeUsernameTextView (pencil) listener
        view.findViewById(R.id.userNameTextView).setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index of right drawables
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // ACTION_DOWN = finger on screen, ACTION_UP = finger on -> off screen
                // getRawX() is where touch is registered, anything on x axis greater than eTRightPosition - 2 * drawableWidth is registered
                if (event.getRawX() >= (v.getRight() - 2 * ((TextView) v).getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (AuthFunctional.currentlyOnline)
                        AuthFunctional.setUpUserNameChange(getActivity());
                    else // if there is no internet, the animated notification quickly flashes
                        AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
                    return true;
                }
            }
            return true;
        });

        // changeEmailTextView (pencil) listener
        view.findViewById(R.id.userEmailTextView).setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index of right drawables
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // ACTION_DOWN = finger on screen, ACTION_UP = finger on -> off screen
                // getRawX() is where touch is registered, anything on x axis greater than eTRightPosition - 2 * drawableWidth is registered
                if (event.getRawX() >= (v.getRight() - 2 * ((TextView) v).getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (AuthFunctional.currentlyOnline)
                        AuthFunctional.setUpEmailChange(getActivity());
                    else // if there is no internet, the animated notification quickly flashes
                        AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
                    return true;
                }
            }
            return true;
        });

        // settingsButton listener - adds a settings fragment
        view.findViewById(R.id.settingsButton).setOnClickListener(view1 -> requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.in_app_container, InAppActivity.settingsFragment).addToBackStack(null).commit());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.profile_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) // loads user's profile pic
            Glide.with(requireActivity()).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into((ImageView) view.findViewById(R.id.profilePicture));
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkManager = new NetworkManager(requireActivity().getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(getActivity(),requireView().findViewById(R.id.profileScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);

        displayCurrentUser();
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(getActivity());
        // removing the listener when activity pauses
        if(authListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }
}
