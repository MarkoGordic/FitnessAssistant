package com.example.fitnessassistant.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.fitnessassistant.R;

public class NotificationController {
    public static void createNotificationChannel(Context context, String channelID, String channelName, String channelDescription, boolean showBadge){
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
        channel.setShowBadge(showBadge);
        channel.setDescription(channelDescription);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    // TODO : Put application logo instead of launcher_foreground
    public static Notification createNotification(Context context, String channelID, String textTitle, String textContent, PendingIntent pendingIntent, boolean cancelNotification, boolean isOngoing, boolean showWhen){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(cancelNotification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(isOngoing)
                .setShowWhen(showWhen);
        return builder.build();
    }
}
