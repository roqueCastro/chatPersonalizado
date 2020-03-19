package com.example.chat.activity;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat.Notifications.Client;
import com.example.chat.Notifications.Data;
import com.example.chat.Notifications.MyResponse;
import com.example.chat.Notifications.Sender;
import com.example.chat.Notifications.Token;
import com.example.chat.R;

import com.example.chat.fragments.APIService;
import com.example.chat.fragments.ChatsFragment;
import com.example.chat.fragments.ProfileFragment;
import com.example.chat.fragments.UsersFragment;
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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main2Activity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    Context context;
    DatabaseReference reference;

    ArrayList<Chat> chats;
    ArrayList<User> users;
    ArrayList<Data> datas;

    String nombre_usuario;

    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.textViewusername);

        context = Main2Activity.this;

        chats = new ArrayList<>();
        users = new ArrayList<>();
        datas = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                nombre_usuario = user.getUsername();
                username.setText(nombre_usuario);

                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher_round);
                }else{
                    if (!Main2Activity.this.isFinishing()) {
                        Glide.with(context).load(user.getImageURL()).into(profile_image);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);



        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**/

                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

                int unread = 0;
                chats.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){



                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReciver().equals(firebaseUser.getUid()) && !chat.getIsseen()){
                        unread++;
                        chats.add(chat);
                    }
                }

                if (unread == 0){
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                }else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "("+unread+") Chats");

                }


                viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

                viewPager.setAdapter(viewPagerAdapter);

                tabLayout.setupWithViewPager(viewPager);

               /* if (chats.size() > 0){
                    sendNotificationRecivier(chats);
                }*/


                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*ENVIO DE NOTIFICACION*/
    private void sendNotificationRecivier(final ArrayList<Chat> chats) {

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
                notificacionesEnviar(chats, users);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void notificacionesEnviar(ArrayList<Chat> chats, ArrayList<User> users) {
        Data data = null;
        datas.clear();

        for (Chat chat : chats){
            /**/
            for (User user: users){
                //
                if (chat.getSender().equals(user.getId())){
                    data = new Data();
                    data.setUser(chat.getSender());
                    data.setIcon(R.mipmap.ic_launcher);
                    data.setMsj(chat.getMessage());
                    data.setNamerecivier(user.getUsername());
                    data.setTitle("");
                    data.setSented(firebaseUser.getUid());
                    datas.add(data);
                }
                //
            }
            /**/
        }

        /*Convertir OBject en string*/
        Gson json = new Gson();

        final String responseDatos = json.toJson(datas);

        /*Convetir object string en jsonObject*/
      /*  Type dataAlType = new TypeToken<ArrayList<Data>>(){}.getType();
        ArrayList<Data> datosResponse = json.fromJson(responseDatos,dataAlType);*/

        //Toast.makeText(getApplicationContext(), "Listo", Toast.LENGTH_SHORT).show();
        clearNotification();
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data("", 0, "", "", "", "", responseDatos);

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


    /*CERRAR NOTIFICACIONES*/
    private void clearNotification() {

        int notificationId = Integer.parseInt(firebaseUser.getUid().replaceAll("[\\D]",  ""));

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }

    /*MENU OPTIONS*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout:

                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(Main2Activity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                startActivity(new Intent(Main2Activity.this, MainActivity.class));
                finish();


                break;

            case R.id.profile:
                startActivity(new Intent(Main2Activity.this, ProfileActivity.class));
                break;
        }

        return false;
    }

    /*CLASE ADAPTADOR VIEWPAGER*/
    class  ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter (FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public  void  addFragment(Fragment fragment, String title ){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    /*STATUD*/
    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    /*APP EN EJECUCCION*/
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    /*APP EN PAUSA O FUERA DE ELLA*/

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
