package com.example.safechat.User;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safechat.Chat.ChatListAdapter;
import com.example.safechat.Chat.ChatObject;
import com.example.safechat.ChatActivity;
import com.example.safechat.FindUserActivity;
import com.example.safechat.LoginActivity;
import com.example.safechat.MainPageActivity;
import com.example.safechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {
    String mName;
    ArrayList<ChatObject> chatList=new ArrayList<>();
    ArrayList<UserObject> userList=new ArrayList<>();
    private RecyclerView.Adapter mChatListAdapter;
    public UserListAdapter(String mName, ArrayList<UserObject>userList){
        this.mName=mName;
        this.userList=userList;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null,false);
        RecyclerView.LayoutParams lp=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        UserListViewHolder rcv=new UserListViewHolder(layoutView);
        getUserChatList();

        return rcv;
    }

    private void getUserChatList(){
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");

        mUserChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String cid;
                        if(FirebaseAuth.getInstance().getUid().compareTo(childSnapshot.getKey())<0)
                            cid=FirebaseAuth.getInstance().getUid()+"_"+childSnapshot.getKey();
                        else
                            cid=childSnapshot.getKey()+"_"+FirebaseAuth.getInstance().getUid();
                        ChatObject mChat = new ChatObject(cid);
                        chatList.add(mChat);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, final int position) {
            holder.mName.setText(userList.get(position).getName());
            holder.mPhone.setText(userList.get(position).getPhone());
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                boolean exists = false;

                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child("chat")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String cid;
                                    if (FirebaseAuth.getInstance().getUid().compareTo(userList.get(position).getUid()) < 0)
                                        cid = FirebaseAuth.getInstance().getUid() + "_" + userList.get(position).getUid();
                                    else
                                        cid = userList.get(position).getUid() + "_" + FirebaseAuth.getInstance().getUid();
                                    int childCounter=0;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if (snapshot.getKey().equals(cid)) {
                                            exists = true;
                                            break;
                                        }
                                        childCounter++;
                                    }
                                    if(childCounter>=dataSnapshot.getChildrenCount()) {
                                        if (!exists) {
                                            FirebaseDatabase.getInstance().getReference().child("chat").child(cid).setValue(true);
                                            FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat").child(userList.get(position).getUid()).setValue(true);
                                            FirebaseDatabase.getInstance().getReference().child("user").child(userList.get(position).getUid()).child("chat").child(FirebaseAuth.getInstance().getUid()).setValue(true);
                                            ChatObject newChat = new ChatObject(cid);
                                            chatList.add(newChat);
                                            mChatListAdapter = new ChatListAdapter(chatList, mName,userList);
                                            mChatListAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                    String cid;
                    if (FirebaseAuth.getInstance().getUid().compareTo(userList.get(position).getUid()) < 0)
                        cid = FirebaseAuth.getInstance().getUid() + "_" + userList.get(position).getUid();
                    else
                        cid = userList.get(position).getUid() + "_" + FirebaseAuth.getInstance().getUid();
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("chatTitle",userList.get(position).getName());
                    bundle.putString("chatID", cid);
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }

            });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserListViewHolder extends RecyclerView.ViewHolder{
        public TextView mName,mPhone;
        public RelativeLayout mLayout;
        public UserListViewHolder(View view){
            super(view);
            mName=view.findViewById(R.id.name);
            mPhone=view.findViewById(R.id.phone);
            mLayout=view.findViewById(R.id.layout);
        }
    }
}
