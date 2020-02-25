package com.example.chat.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.chat.Adapter.UserAdapter;
import com.example.chat.R;
import com.example.chat.activity.Main2Activity;
import com.example.chat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.security.AccessController.getContext;


public class UsersFragment extends Fragment {

    private View view;
    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> users;

    private EditText search_user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        users = new ArrayList<>();

        readUsers();

        search_user = view.findViewById(R.id.search_user);
        search_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /**/
                try{

                    ArrayList<User> listaFiltrada = filter((ArrayList<User>) users, s.toString());

                    userAdapter = new UserAdapter(getContext(), listaFiltrada, false, s.toString());
                    userAdapter.notifyDataSetChanged();

                    recyclerView.setAdapter(userAdapter);

                }catch (Exception e){
                    e.printStackTrace();
                }
                /**/
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    /*BUSCAR USUARIOS*/

    //METODO FILTRO
   /* public void setFilter(ArrayList<User> listaUsuarios){
        this.users = new ArrayList<>();
        this.users.addAll(listaUsuarios);
    }*/

    //NOW
    private ArrayList<User> filter (ArrayList<User> listUsers, String texto){
        ArrayList<User>  listaFiltrada =new ArrayList<>();

       /* try {
            texto = texto.toLowerCase();

            for (User user : listUsers){
                String usu = user.getUsername();

                if (usu.contains(texto)){
                    listaFiltrada.add(user);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }*/
        try {
            texto = texto.toLowerCase();

            for (User user : listUsers){
                String usu = user.getUsername();

                String aguja = "sql";             //palabra buscada
                String pajar = "lenguaje SQL";    //texto


                //Pattern select = Pattern.compile("\\b" + Pattern.quote(texto) + "\\b", Pattern.CASE_INSENSITIVE);
                Pattern select = Pattern.compile( Pattern.quote(texto) , Pattern.CASE_INSENSITIVE);
                Matcher operation = select.matcher(usu);


                if (operation.find()) {
                    listaFiltrada.add(user);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return listaFiltrada;
    }

    //BEFORE
    private void searchUser(String s) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**/
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert firebaseUser != null;
                    if (!user.getId().equals(firebaseUser.getUid())){
                        users.add(user);
                    }
                }

                userAdapter = new UserAdapter(getContext(), users, false, null);
                recyclerView.setAdapter(userAdapter);
                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*LEER USUARIOS*/
    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                /**/

                if (search_user.getText().toString().equals("")){
                    users.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid())){
                            users.add(user);
                        }
                    }

                    userAdapter = new UserAdapter(getContext(), users, false, null);
                    recyclerView.setAdapter(userAdapter);
                }

                /**/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
