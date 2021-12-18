package com.example.fitnessassistant.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.authentication.SignInActivity;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilePageFragment extends Fragment {
    // called in onResume -> reloading user and displaying user info
    private void displayCurrentUser(View view){
        // setting up current user - this is in onResume in case anything gets changed after onPause()
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(true);
            // try to reload the user (update) to get the latest data
            if(AuthFunctional.currentlyOnline)
                currentUser.reload().addOnCompleteListener(task -> {
                    ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);
                    if(task.isSuccessful()) // we're displaying the user's profile (reloaded)
                        AuthFunctional.dispUser(view, currentUser);
                    else
                        try{
                            if(task.getException() != null)
                                throw task.getException();
                        } catch (FirebaseNetworkException e1){ // if it fails and it's a network error, the animated notification quickly flashes
                            AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
                            // we're displaying the user's profile (last saved)
                            AuthFunctional.dispUser(view, currentUser);
                        } catch(Exception e2){ // if it fails and we're online(user deleted, disabled or credentials no longer valid) -> return to sign in
                            startActivity(new Intent(getActivity(), SignInActivity.class));
                            requireActivity().finish();
                        }
                });
            else{ // quick flash the notification
                ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
                // we're displaying the user's profile (last saved)
                AuthFunctional.dispUser(view, currentUser);
            }
        }
    }

    // setting up oCListeners
    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClickListeners(View view) {
        // swipeRefreshLayout refresh listener - refreshes for 1.5s while updating UI
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setOnRefreshListener(() -> displayCurrentUser(view));
                // settingsButton listener - adds a settings fragment, hides current
                view.findViewById(R.id.settingsButton).setOnClickListener(view1 -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.settingsFragment).addToBackStack(null).commit());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.profile_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) { // loads user's profile pic
            Glide.with(requireActivity()).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into((ImageView) view.findViewById(R.id.profilePicture));
            displayCurrentUser(view);
        }

        // if we got back from dark/light mode switched we go to the settings
        if(requireActivity().getSharedPreferences("ui_preferences", Context.MODE_PRIVATE).getBoolean("mode_changed", false)){
            ((BottomNavigationView) requireActivity().findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.profile);
            view.findViewById(R.id.settingsButton).performClick();
            SharedPreferences prefs = requireActivity().getSharedPreferences("ui_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("ui_preferences", false);
            editor.apply();
        }
        return view;
    }
}
