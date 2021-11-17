package com.example.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

// Class is used for monitoring connection state by extending LiveData
public class ConnectionStateMonitor extends LiveData<Boolean> {
    private final ConnectivityManager.NetworkCallback networkCallback;
    private final ConnectivityManager connectivityManager;

    public ConnectionStateMonitor(Context context){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new NetworkCallback(this);
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateConnection();
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    private static class NetworkCallback extends ConnectivityManager.NetworkCallback {
        private final ConnectionStateMonitor connectionStateMonitor;

        public NetworkCallback(ConnectionStateMonitor connectionStateMonitor){
            this.connectionStateMonitor = connectionStateMonitor;
        }

        // called when network connection is available
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            if (connectionStateMonitor.getValue() != null && connectionStateMonitor.getValue()) return;
            new TaskRunner().executeAsync(new CheckForConnectionTask(), connectionStateMonitor::postValue);
        }

        // called when network connection is lost
        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            connectionStateMonitor.postValue(false);
        }

        // called when network connection is unavailable
        @Override
        public void onUnavailable() {
            super.onUnavailable();
            connectionStateMonitor.postValue(false);
        }
    }

    private void updateConnection(){
        if(connectivityManager != null){
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if(activeNetwork != null && activeNetwork.isConnected()){
                if(getValue() != null && getValue()) return;
                new TaskRunner().executeAsync(new CheckForConnectionTask(), this::postValue);
            } else
                postValue(false);
        }
    }

    // used instead of AsyncTask
    private static class TaskRunner{
        private interface Callback<R>{
            void onComplete(R result);
        }

        // custom TaskRunner is used because AsyncTask is deprecated
        public <R> void executeAsync(Callable<R> callable,Callback<R> callback){
            // Executors.newSingleThreadExecutor() creates a single thread to execute the code
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    final R result = callable.call();
                    // create a handler for the Looper.getMainLooper() - main(UI) thread
                    //  attaches the code to be worked on the thread
                    new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(result));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class CheckForConnectionTask implements Callable<Boolean>{
        @Override
        public Boolean call(){
            // returns if connecting to Google server "8.8.8.8" has been successful
            // used to check if there really is internet for some specific cases
            try{
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500); // throws
                sock.close();
                return true;
            } catch(IOException e){ return false; }
        }
    }
}