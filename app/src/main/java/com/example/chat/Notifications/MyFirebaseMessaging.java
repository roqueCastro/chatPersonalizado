package com.example.chat.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


import com.example.chat.R;
import com.example.chat.activity.MessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");
        String userid = remoteMessage.getData().get("user");

        SharedPreferences  sharedPreferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentuser = sharedPreferences.getString("currentuser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sented.equals(firebaseUser.getUid())){

            //Notificacion si esta por fuera del chat
            if (!currentuser.equals(userid)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    senderOreoNotification(remoteMessage);
                }else {
                    sendNotification(remoteMessage);
                }
            }

        }
    }

    private void senderOreoNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("msj");
        String usu = remoteMessage.getData().get("namerecivier");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]",  ""));
        Intent intent = new Intent(this, MessageActivity.class);

        intent.putExtra("userid", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = (PendingIntent) PendingIntent.getActivities(this, j, new Intent[]{intent}, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(usu,body,pendingIntent, defaultSound, icon);

        int i = 0;
        if(j > 0){
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("msj");
        String usu = remoteMessage.getData().get("namerecivier");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]",  ""));
        Intent intent = new Intent(this, MessageActivity.class);

        intent.putExtra("userid", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = (PendingIntent) PendingIntent.getActivities(this, j, new Intent[]{intent}, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(usu)
                .setContentText(body)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        int i = 0;
        if(j > 0){
            i = j;
        }
        noti.notify(i, builder.build());
    }
}
