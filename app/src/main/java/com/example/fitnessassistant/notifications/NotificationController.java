package com.example.fitnessassistant.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fitnessassistant.R;

public class NotificationController {
    public static void createNotificationChannel(Context context, String channelName, String channelDescription){
        int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("Pedometer", channelName, importance);
        channel.setDescription(channelDescription);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    // TODO : Put application logo instead of launcher_foreground
    public static void pushNotification(Context context, String channelID, String textTitle, String textContent, PendingIntent pendingIntent, boolean cancelNotification, int notificationID, boolean isOngoing, boolean showWhen){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle((CharSequence) textTitle)
                .setContentText((CharSequence) textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(cancelNotification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(isOngoing)
                .setShowWhen(showWhen);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationID, builder.build());
    }
}
