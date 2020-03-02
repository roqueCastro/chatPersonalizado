package com.example.chat.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.Person;
import android.util.Log;


import com.example.chat.R;
import com.example.chat.activity.Main2Activity;
import com.example.chat.activity.MessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    ArrayList<Data> datos;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*Convetir object string en jsonObject*/
        String responseDatos = remoteMessage.getData().get("dato");

        Gson json = new Gson();
        Type dataAlType = new TypeToken<ArrayList<Data>>(){}.getType();

        datos = json.fromJson(responseDatos,dataAlType);

        String sented = remoteMessage.getData().get("sented");
        String userid = remoteMessage.getData().get("user");

        SharedPreferences  sharedPreferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentuser = sharedPreferences.getString("currentuser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sented.equals(firebaseUser.getUid())){

            //Notificacion si esta por fuera del chat
            if (!currentuser.equals(userid)){

                clearNotification(userid);

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
//        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]",  ""));
        PendingIntent launchIntent = getLaunchIntent(user, getBaseContext());
//        Intent intent = new Intent(this, MessageActivity.class);
//
//        intent.putExtra("userid", user);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = (PendingIntent) PendingIntent.getActivities(this, j, new Intent[]{intent}, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        NotificationCompat.Builder builder = oreoNotification.getOreoNotification();


        /*NOTIFICATION CHATS*/
        NotificationCompat.MessagingStyle messagingStyle = new
                NotificationCompat.MessagingStyle("chatApp").setConversationTitle("Group && Chats");

        if (datos != null){
            Log.d("LOG: ","list size " + datos.size());
            for(Data databaseMessage : datos){
                NotificationCompat.MessagingStyle.Message notificationMessage  = new
                        NotificationCompat.MessagingStyle.Message(
                        databaseMessage.getMsj(),
                        0,
                        databaseMessage.getNamerecivier()
                );
                messagingStyle.addMessage(notificationMessage);
            }
        }


        builder.setSmallIcon(R.drawable.ic_message);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
        builder.setContentTitle("Messages");
        builder.setStyle(messagingStyle);

        builder.setAutoCancel(true);
        builder.setContentIntent(launchIntent);
        builder.setSound(defaultSound);


        int i = 0;
        if(j > 0){
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");

        int NOTIFICATION_ID = Integer.parseInt(user.replaceAll("[\\D]",  ""));

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        PendingIntent launchIntent = getLaunchIntent(user, getBaseContext());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        /*NOTIFICATION CHATS*/
        NotificationCompat.MessagingStyle messagingStyle = new
                NotificationCompat.MessagingStyle("chatApp").setConversationTitle("Group && Chats");

        if (datos != null){
            Log.d("LOG: ","list size " + datos.size());
            for(Data databaseMessage : datos){
                NotificationCompat.MessagingStyle.Message notificationMessage  = new
                        NotificationCompat.MessagingStyle.Message(
                        databaseMessage.getMsj(),
                        0,
                        databaseMessage.getNamerecivier()
                );
                messagingStyle.addMessage(notificationMessage);
            }
        }

        builder.setSmallIcon(R.drawable.ic_message);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
        builder.setContentTitle("Messages");
        builder.setStyle(messagingStyle);

        builder.setAutoCancel(true);
        builder.setContentIntent(launchIntent);
        builder.setSound(defaultSound);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

       /* RemoteMessage.Notification notification = remoteMessage.getNotification();
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
        noti.notify(i, builder.build());*/
    }

    public PendingIntent getLaunchIntent(String notificationId, Context context) {
        String usu_enviar = "";
        int vent_envio = 0;

        for (Data data : datos){

            if (usu_enviar == ""){
                usu_enviar = data.getSented();
            }else{
                if(usu_enviar.equals(data.getSented())){

                }else {
                    vent_envio = 1;
                }
            }
        }


        if (vent_envio == 0){
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userid", usu_enviar);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }else {
            Intent intent = new Intent(context, Main2Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

    }

    /*CERRAR NOTIFICACIONES*/
    private void clearNotification(String userid) {

     //   int notificationId = Integer.parseInt(userid.replaceAll("[\\D]",  ""));

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }
}
