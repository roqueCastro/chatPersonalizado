package com.example.chat.Notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import retrofit2.http.Body;

public class OreoNotification extends ContextWrapper {

    private static final  String CHANNEL_ID = "com.example.chat";
    private static final  String CHANNEL_NAME = "chat";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        if (notificationManager != null){
            notificationManager = (NotificationManager)  getSystemService(Context.NOTIFICATION_SERVICE);
        }

        getManager().createNotificationChannel(channel);


    }

    public NotificationManager getManager(){
        if (notificationManager == null){
            notificationManager = (NotificationManager)  getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public NotificationCompat.Builder getOreoNotification (){
        return new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID);

       /* return  new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(Integer.parseInt(icon))
                .setSound(soundUri)
                .setAutoCancel(true);*/
    }

}
