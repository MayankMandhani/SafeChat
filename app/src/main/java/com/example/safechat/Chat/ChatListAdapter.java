package com.example.safechat.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safechat.ChatActivity;
import com.example.safechat.R;
import com.example.safechat.User.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    ArrayList<ChatObject> chatList;
    ArrayList<UserObject> userList;
    String mName;
    public ChatListAdapter(ArrayList<ChatObject> chatList, String mName,
                           ArrayList<UserObject> userList){
        this.chatList=chatList;
        this.mName=mName;
        this.userList=userList;
    }
    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null,false);
        RecyclerView.LayoutParams lp=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ChatListViewHolder rcv=new ChatListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, final int position) {
        String cid=chatList.get(position).getChatId();
        String uid;
        if(cid.startsWith(FirebaseAuth.getInstance().getUid()))
            uid=cid.substring(cid.indexOf('_')+1);
        else
            uid=cid.substring(0,cid.indexOf('_'));
        String uname="";
        for(UserObject userObject:userList){
            if(userObject.getUid().equals(uid))
            {
                FirebaseDatabase.getInstance().getReference().child(uid);
                uname=uname.concat(userObject.getName());
                break;
            }
        }
        if(uname.equals("")) {
            FirebaseDatabase.getInstance().getReference().child("user")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String cid=chatList.get(position).getChatId();
                            String uid;
                            if(cid.startsWith(FirebaseAuth.getInstance().getUid()))
                                uid=cid.substring(cid.indexOf('_')+1);
                            else
                                uid=cid.substring(0,cid.indexOf('_'));
                            for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
                                if(childSnapshot.getKey().equals(uid)){
                                    holder.mTitle.setText(childSnapshot.child("name").getValue().toString());
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
        }
        else
            holder.mTitle.setText(uname);
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), ChatActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("chatID",chatList.get(holder.getAdapterPosition()).getChatId());
                bundle.putString("mName",mName);
                String cid=chatList.get(position).getChatId();
                String uid;
                if(cid.startsWith(FirebaseAuth.getInstance().getUid()))
                    uid=cid.substring(cid.indexOf('_')+1);
                else
                    uid=cid.substring(0,cid.indexOf('_'));
                String uname="";
                for(UserObject userObject:userList){
                    if(userObject.getUid().equals(uid))
                    {
                        uname=uname.concat(userObject.getName());
                        break;
                    }
                }
                bundle.putString("chatTitle",uname);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override

    public int getItemCount() {
        return chatList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder{
        public TextView mTitle;
        public LinearLayout mLayout;
        public ChatListViewHolder(View view){
            super(view);
            mTitle=view.findViewById(R.id.title);
            mLayout=view.findViewById(R.id.layout);
        }
    }
}
