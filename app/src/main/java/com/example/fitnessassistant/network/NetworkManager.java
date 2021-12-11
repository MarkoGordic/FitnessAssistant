package com.example.fitnessassistant.network;

import android.app.Application;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;

import com.example.fitnessassistant.util.AuthFunctional;

// used to notify user about networkState with usage of com.example.fitnessassistant.network.ConnectionStateMonitor class
public class NetworkManager {
    private final ConnectionStateMonitor csMonitor;

    // called in onCreate() -> declaring networkManager as a field
    public NetworkManager(Application application){
        csMonitor = new ConnectionStateMonitor(application);
    }

    // called in onResume() - registering
    public void registerConnectionObserver(LifecycleOwner lifecycleOwner, TextView notificationView){
        csMonitor.observe(lifecycleOwner, connected -> {
            if(!connected){
                // if we're offline notification is visible and currentlyOnline is false
                notificationView.setVisibility(View.VISIBLE);
                AuthFunctional.currentlyOnline = false;
            } else{
                // if we're online notification is not present and currentlyOnline is true
                notificationView.setVisibility(View.GONE);
                AuthFunctional.currentlyOnline = true;
            }
        });
    }

    // called in onPause() - unregistering
    public void unregisterConnectionObserver(LifecycleOwner lifecycleOwner){
        csMonitor.removeObservers(lifecycleOwner);
    }
}