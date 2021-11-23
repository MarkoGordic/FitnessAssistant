package com.example.fitnessassistant.util;

import android.app.Activity;
import android.app.AlertDialog;

import androidx.core.app.ActivityCompat;


public class PermissionFunctional {
    // requests the permission
    private static void requestPermission(Activity activity, String permissionName, int permissionRequestCode){
        ActivityCompat.requestPermissions(activity, new String[]{ permissionName }, permissionRequestCode);
    }

    // creates an alert dialog with rationale shown
    private static void showExplanation(Activity activity, String title, String message, String permission, int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, (dialogInterface, i) -> requestPermission(activity, permission, requestCode));
        builder.create().show();
    }

    // checks if the rationale should be shown
    public static void askForPermission(Activity activity, String title, String message, String permission, int requestCode){
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            showExplanation(activity, title , message, permission, requestCode);
        else
            requestPermission(activity, permission, requestCode);
    }
}
