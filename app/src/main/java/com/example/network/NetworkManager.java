package com.example.network;

import android.app.Application;

import androidx.lifecycle.LifecycleOwner;

// used to notify user about networkState with usage of com.example.network.ConnectionStateMonitor class
public class NetworkManager {
    private final ConnectionStateMonitor csMonitor;

    public NetworkManager(Application application){
        csMonitor = new ConnectionStateMonitor(application);
    }

    public void registerConnectionObserver(LifecycleOwner lifecycleOwner){
        csMonitor.observe(lifecycleOwner, isConnected -> {
            if(isConnected)
                System.out.println("NetworkState: CONNECTED"); // TODO Create a notificationView
            else
                System.out.println("NetworkState: NOT CONNECTED");
        });
    }

    public void unregisterConnectionObserver(LifecycleOwner lifecycleOwner){
        csMonitor.removeObservers(lifecycleOwner);
    }
}
