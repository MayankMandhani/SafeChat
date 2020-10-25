package com.example.safechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.safechat.Chat.ChatListAdapter;
import com.example.safechat.Chat.ChatObject;
import com.example.safechat.User.UserListAdapter;
import com.example.safechat.User.UserObject;
import com.example.safechat.Utils.CountryToPhonePrefix;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {

    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;
    String mName;
    ArrayList<ChatObject> chatList;
    ArrayList<UserObject> userList;
    ArrayList<UserObject> contactList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        mName=getIntent().getExtras().getString("mName");
        Button mLogout=findViewById(R.id.logout);
        Button mFindUser=findViewById(R.id.findUser);
        mFindUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(), FindUserActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("mName",mName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        getPermission();
        initializeRecyclerView();
        getUserChatList();
        getContactList();
    }

    private void getContactList(){

        String ISOPrefix=getCountryISO();
        Cursor phones= getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        while(phones.moveToNext()){
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone=phone.replace(" ","");
            phone=phone.replace("-","");
            phone=phone.replace("(","");
            phone=phone.replace(")","");
            if(!String.valueOf(phone.charAt(0)).equals("+")) {
                if (String.valueOf(phone.charAt(0)).equals("0"))
                    phone = phone.substring(1);
                phone = ISOPrefix + phone;
            }
            UserObject mContact=new UserObject("",name,phone);
            contactList.add(mContact);
            getUserList(mContact);
        }
    }

    private void getUserList(final UserObject mContact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "", name = "";
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.child("phone").getValue() != null)
                            phone = childSnapshot.child("phone").getValue().toString();
                        if (childSnapshot.child("name").getValue() != null)
                            name = childSnapshot.child("name").getValue().toString();
                        UserObject mUser = new UserObject(childSnapshot.getKey(), name, phone);
                        for (UserObject mContactIterator : contactList) {
                            if (mContactIterator.getPhone().equals(phone)) {
                                mUser.setName(mContactIterator.getName());
                                FirebaseDatabase.getInstance().getReference().child("user").child(childSnapshot.getKey()).child("name").setValue(name);
                                break;
                            }
                        }
                        userList.add(mUser);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getCountryISO(){
        String iso=null;
        TelephonyManager telephonyManager=(TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso()!=null)
            if(!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso=telephonyManager.getNetworkCountryIso().toString();

        return CountryToPhonePrefix.getPhone(iso);
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
                                boolean exists=false;
                                for(ChatObject chatObject:chatList){
                                    if(cid.equals(chatObject.getChatId()))
                                    {
                                        exists=true;
                                        break;
                                    }
                                }
                                if(!exists) {
                                    chatList.add(mChat);
                                    mChatListAdapter.notifyDataSetChanged();
                                }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }
    private void initializeRecyclerView() {
        userList=new ArrayList<>();
        contactList=new ArrayList<>();
        chatList=new ArrayList<>();
        mChatList=findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled (false);
        mChatList.setHasFixedSize(false);
        mChatListLayoutManager=new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL,false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter=new ChatListAdapter(chatList,mName,userList);
        mChatList.setAdapter(mChatListAdapter);
    }

    private void getPermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }
}
