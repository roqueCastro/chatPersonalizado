package com.example.chat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.activity.MessageActivity;
import com.example.chat.model.Chat;
import com.example.chat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private Boolean ischat;
    private String busqueda;


    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, Boolean ischat, String busqueda) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.ischat = ischat;
        this.busqueda = busqueda;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        final User user = mUsers.get(i);

        // PONERLE COLOR AL TEXTO EN LA BUSQUEDA
        if (busqueda != null){
            String notes = user.getUsername();

            SpannableStringBuilder sb = new SpannableStringBuilder(notes);
            Pattern p = Pattern.compile(busqueda, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(notes);
            while (m.find()){
               // sb.setSpan(new BackgroundColorSpan(Color.BLUE), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(Color.BLUE), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                //sb.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

            holder.username.setText(sb);
        }else {
            holder.username.setText(user.getUsername());
        }


        if (user.getImageURL().equals("default")){
            holder.profile_name.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_name);
        }

        if (ischat){
            lastMessage(user.getId(), holder.last_msg);
        }else{
            holder.last_msg.setVisibility(View.GONE);
        }


        if (ischat){
            if (user.getStatus().equals("online")){
                holder.imgon.setVisibility(View.VISIBLE);
                holder.imgoff.setVisibility(View.GONE);
            }else {
                holder.imgoff.setVisibility(View.VISIBLE);
                holder.imgon.setVisibility(View.GONE);
            }
        }else {
            holder.imgoff.setVisibility(View.GONE);
            holder.imgon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username, last_msg;
        public ImageView profile_name;
        private ImageView imgon, imgoff;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username_ui);
            profile_name = itemView.findViewById(R.id.profile_image_ui);
            imgon = itemView.findViewById(R.id.img_on);
            imgoff = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    private void lastMessage(final String userid, final TextView last_msg ){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**/

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if (firebaseUser == null){
                        theLastMessage = "default";
                    }else {
                        if (chat.getReciver().equals(firebaseUser.getUid())
                                && chat.getSender().equals(userid)
                                || chat.getReciver().equals(userid)
                                && chat.getSender().equals(firebaseUser.getUid())
                        ){
                            theLastMessage = chat.getMessage();
                        }
                    }

                }

                switch (theLastMessage){
                    case "default":
                        last_msg.setText("No message.");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                }


                theLastMessage = "default";

                /**/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*CAMBIADOR A NEGRITA*/
  /*  public SpannableStringBuilder append(CharSequence text, Object what, int flags) {
        int start = length();
        append(text);
        setSpan(what, start, length(), flags);
        return this;
    }*/
}
