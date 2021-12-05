package com.example.fitnessassistant.diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.firebase.auth.FirebaseAuth;


public class DiaryPageFragment extends Fragment {
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // returning the view
        return inflater.inflate(R.layout.diary_screen, container, false);
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
        // registering when user comes first time or returns
        networkManager.registerConnectionObserver(getActivity(), requireView().findViewById(R.id.diaryScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregistering when this goes into background or else
        networkManager.unregisterConnectionObserver(getActivity());
        // removing the listener
        if(authListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }
}
