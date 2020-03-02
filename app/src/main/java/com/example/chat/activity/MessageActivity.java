package com.example.chat.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.FontRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.chat.Adapter.MessageAdapter;
import com.example.chat.Notifications.Client;
import com.example.chat.Notifications.Data;
import com.example.chat.Notifications.MyResponse;
import com.example.chat.Notifications.Sender;
import com.example.chat.Notifications.Token;
import com.example.chat.R;
import com.example.chat.fragments.APIService;
import com.example.chat.model.Chat;
import com.example.chat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference, refSeen;

    Intent intent;

    ImageButton send;
    EditText message;

    MessageAdapter messageAdapter;
    List<Chat> chats;

    ArrayList<Chat> chatsN;
    ArrayList<User> users;
    ArrayList<Data> datas;
    ArrayList<String> listaUserMessaging;

    RecyclerView recyclerView;

    ValueEventListener seenListener;

    APIService apiService;
    Boolean notify = false;

    String userid;

    int notifi_envio = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar_m);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MessageActivity.this, Main2Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });


        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        chatsN = new ArrayList<>();
        users = new ArrayList<>();
        datas = new ArrayList<>();
        listaUserMessaging = new ArrayList<>();


        profile_image = findViewById(R.id.profile_image_m);
        username = findViewById(R.id.textViewusername_m);
        message = findViewById(R.id.text_send);
        send = findViewById(R.id.btn_imageview_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;
                String msj = message.getText().toString();
                if (!msj.equals("")){
                    sendMessage(firebaseUser.getUid(), userid, msj);
                    message.setText("");
                }else {
                    Toast.makeText(getApplicationContext(), "Tu mensaje esta vacio.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**/

                User user = dataSnapshot.getValue(User.class);

                username.setText(user.getUsername());

                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                readMessage(firebaseUser.getUid(), userid, user.getImageURL());

                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**/
        /**/
        recyclerView = findViewById(R.id.recycler_view_m);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        seenMessage(userid);
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    /*VISTO MESSAGE*/
    private void seenMessage(final String userid){
        refSeen = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = refSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)    {
                /**/

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReciver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }

                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*ENVIO DE MENSAJE */
    private  void  sendMessage(String sender, final String reciver, String message){

        notifi_envio = 1;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("reciver", reciver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(reciver);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**/
                chatsNoti(reciver);

                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(reciver);
                }

                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /**************************************SEND NOTIFICACION*******************/



      //  final String msg = message;

       /* reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                *//**//*

                User user = dataSnapshot.getValue(User.class);

                if (notify) {
                    sendNotificationRecivier(reciver, user.getUsername(), msg);
                }
                notify = false;

                *//**//*
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

    }

    /*ENVIO DE NOTIFICACION*/
    private void sendNotificationRecivier(final ArrayList<Chat> chatsN, final String reciver) {

        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                users.clear();
                for (DataSnapshot objSnapshot : dataSnapshot.getChildren()){
                    User user = objSnapshot.getValue(User.class);
                    users.add(user);
                }


                //Toast.makeText(getApplicationContext(), "Listo", Toast.LENGTH_SHORT).show();\
                notificacionesEnviar(chatsN, users, reciver);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //COMPROVANTE ENVIO NOTIFICACION
    private void notificacionesEnviar(ArrayList<Chat> chatsN, ArrayList<User> users, String reciver) {
        if (notifi_envio == 1){
            notifi_envio = 0;


            String usu_enviar = "";


            Data data = null;
            datas.clear();
            listaUserMessaging.clear();

            for (Chat chat : chatsN){
                /**/
                //ACOMODAR LISTA DE MENSAJES ORDENADAMENTE
                if (usu_enviar == ""){
                    usu_enviar = chat.getSender();
                    listaUserMessaging.add(chat.getSender());
                }else{
                    if(usu_enviar.equals(chat.getSender())){

                    }else {
//                        vent_envio = 1;
                        listaUserMessaging.add(chat.getSender());
                    }
                }
                /**/
            }

            /*AGREGAR MESSAGES*/
            for (int i = 0; i<listaUserMessaging.size(); i++){
                /*1*/
                for (Chat chat : chatsN){
                    /*2*/
                    if(listaUserMessaging.get(i).equals(chat.getSender())){
                        /*3*/
                         for (User user: users){
                             /*4*/
                            if (chat.getSender().equals(user.getId())){
                                /*5*/
                                data = new Data();
                                data.setUser(chat.getSender());
                                data.setIcon(R.mipmap.ic_launcher);
                                data.setMsj(chat.getMessage());
                                data.setNamerecivier(user.getUsername());
                                data.setTitle("");
                                data.setSented(firebaseUser.getUid());
                                datas.add(data);
                                /*5*/
                            }
                            /*4*/
                        }
                        /*3*/
                    }
                    /*2*/
                }
                /*1*/
            }
            /*0*/

            /*Convertir OBject en string*/
            Gson json = new Gson();

            final String responseDatos = json.toJson(datas);

            /*Convetir object string en jsonObject*/
      /*  Type dataAlType = new TypeToken<ArrayList<Data>>(){}.getType();
        ArrayList<Data> datosResponse = json.fromJson(responseDatos,dataAlType);*/

            //Toast.makeText(getApplicationContext(), "Listo", Toast.LENGTH_SHORT).show();
            /// clearNotification();
            DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
            Query query = tokens.orderByKey().equalTo(reciver);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Token token = snapshot.getValue(Token.class);
                        Data data = new Data(firebaseUser.getUid(), 0, "", "", "", userid, responseDatos);

                        Sender sender = new Sender(data, token.getToken());
                        // Toast.makeText(getApplicationContext(), "Failds Noti", Toast.LENGTH_SHORT).show();
                        apiService.sendNotification(sender)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {



                                        if (response.code() == 200){
                                            if (response.body().success ==1){
                                                //Notificacion no es enviada al otro usuario
//                                            Toast.makeText(getApplicationContext(), "Failds Noti", Toast.LENGTH_SHORT).show();
                                            }
                                        }


                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                        Toast.makeText(getApplicationContext(), "Error->\n" + call.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }
    }

    //CHAT NOTIFICACION
    public void  chatsNoti (final String reciver){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**/

                chatsN.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){



                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReciver().equals(userid) && !chat.getIsseen()){
                        chatsN.add(chat);
                    }
                }

                sendNotificationRecivier(chatsN, reciver);
                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /******************************************************************************************/

    //NULA NO SE UTILIZA.
    private void sendNotificationRecivier(String reciver, final String username, final String msg) {
        /*DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(reciver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                *//**//*

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, msg, username, "", userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                    .enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            *//**//*

                            if (response.code() == 200){
                                if (response.body().success ==1){
                                    //Notificacion no es enviada al otro usuario
//                                            Toast.makeText(getApplicationContext(), "Failds Noti", Toast.LENGTH_SHORT).show();
                                }
                            }

                            *//**//*
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                        }
                    });

                }

                *//**//*
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    /*LEER MENSAJES*/
    private  void readMessage(final String myid, final String userid, final String imagenurl){
        chats = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**/
                chats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReciver().equals(myid)
                            && chat.getSender().equals(userid)
                            || chat.getReciver().equals(userid)
                            && chat.getSender().equals(myid)
                    ){

                        chats.add(chat);
                    }


                }

                messageAdapter = new MessageAdapter(getApplicationContext(), chats, imagenurl);
                recyclerView.setAdapter(messageAdapter);
                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*STATUD*/
    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }


    /*CERRAR NOTIFICACIONES*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void clearNotification(String userid) {

        //int notificationId = Integer.parseInt(userid.replaceAll("[\\D]",  ""));

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null){
           // Toast.makeText(getApplicationContext(), "ON_RESUM",Toast.LENGTH_SHORT).show();
            manager.cancelAll();

        }

    }

    /*APP EN EJECUCCION*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();


        clearNotification(userid);

        //STATUS EN LINEA//
        status("online");

        //USUARIO EN EL CHAT PARA NO ENVIAR LA NOTIFICACION//
        currentUser(userid);
       // Toast.makeText(getApplicationContext(), "ON_RESUM",Toast.LENGTH_SHORT).show();
    }

    /*APP EN PAUSA O FUERA DE ELLA*/
    @Override
    protected void onPause() {
        super.onPause();
        refSeen.removeEventListener(seenListener);//Elliminar el evento de la databaseReference..
        status("offline");
        currentUser("none");
        //Toast.makeText(getApplicationContext(), "ON_PAUSE",Toast.LENGTH_SHORT).show();
    }

}
