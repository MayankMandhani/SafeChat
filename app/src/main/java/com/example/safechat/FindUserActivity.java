package com.example.safechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

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

import java.util.ArrayList;


public class FindUserActivity extends AppCompatActivity {

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    String mName;
    ArrayList<UserObject> contactList;
    ArrayList<UserObject> userList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mName=getIntent().getExtras().getString("mName");
        setContentView(R.layout.activity_find_user);
        contactList=new ArrayList<>();
        userList=new ArrayList<>();
        initializeRecyclerView();
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
                getUserDetails(mContact);
        }
    }

    private void getUserDetails(final UserObject mContact) {
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
                            boolean exists=false;
                            for(UserObject userObject:userList){
                                if(userObject.getPhone().equals(mUser.getPhone())){
                                    exists=true;
                                    break;
                                }
                            }
                            if (!exists) {
                                userList.add(mUser);
                                mUserListAdapter.notifyDataSetChanged();
                            }
                            return;
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
    private void initializeRecyclerView() {
        mUserList=findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled (false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager=new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL,false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter=new UserListAdapter(mName,userList);
        mUserList.setAdapter(mUserListAdapter);
    }
}