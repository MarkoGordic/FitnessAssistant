package com.example.fitnessassistant.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class NotificationController {
    public static void createNotificationChannelGroup(Context context, String groupName, String groupID){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(groupID, groupName));
    }

    public static void createNotificationChannel(Context context, String channelID, String channelName, String channelDescription, boolean showBadge, int importance){
        NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
        channel.setShowBadge(showBadge);
        channel.setDescription(channelDescription);

        switch (channelID) {
            case "SleepTracker":
            case "SleepTrackerAlerts":
                channel.setGroup("sleep_tracker");
                break;
            case "Pedometer":
                channel.setGroup("pedometer");
                break;
            case "ActivityTracking":
                channel.setGroup("activity_tracking");
                break;
        }

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification createNotification(Context context, String channelID, String textTitle, String textContent, PendingIntent pendingIntent, boolean cancelNotification, boolean isOngoing, boolean showWhen, int icon){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(icon)
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
