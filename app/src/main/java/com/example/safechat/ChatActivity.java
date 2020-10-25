package com.example.safechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.safechat.Chat.MessageAdapter;
import com.example.safechat.Chat.ChatObject;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import com.example.safechat.CryptoUtils;
import com.example.safechat.Chat.MessageObject;
import com.example.safechat.User.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChatActivity extends AppCompatActivity {
    private RecyclerView mChat;
    private TextView mChatTitle;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;
    ArrayList<MessageObject> messageList;
    DatabaseReference mChatDb;
    SecretKey secretKey;
    String chatID;
    String mName;
    String chatTitle;

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int AES_KEY_BIT = 256;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    // AES-GCM needs GCMParameterSpec
    public static byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] encryptedText = cipher.doFinal(pText);
        return encryptedText;

    }

    // prefix IV length + IV bytes to cipher text
    public static byte[] encryptWithPrefixIV(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

        byte[] cipherText = encrypt(pText, secret, iv);

        byte[] cipherTextWithIv = ByteBuffer.allocate(iv.length + cipherText.length)
                .put(iv)
                .put(cipherText)
                .array();
        return cipherTextWithIv;

    }

    public static String decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] plainText = cipher.doFinal(cText);
        return new String(plainText, UTF_8);

    }

    public static String decryptWithPrefixIV(byte[] cText, SecretKey secret) throws Exception {

        ByteBuffer bb = ByteBuffer.wrap(cText);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        bb.get(iv);
        //bb.get(iv, 0, iv.length);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        String plainText = decrypt(cipherText, secret, iv);
        return plainText;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatID=getIntent().getExtras().getString("chatID");
        mName=getIntent().getExtras().getString("mName");
        chatTitle=getIntent().getExtras().getString("chatTitle");
        mChatDb=FirebaseDatabase.getInstance().getReference().child("chat").child(chatID);
        getSupportActionBar().setTitle(chatTitle);
        Button mSend=findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        initializeRecyclerView();
        getChatMessages();
    }

    private void getChatMessages() {
        mChatDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    if (dataSnapshot.exists()) {
                        String text = "", creatorID = "", timestamp="";

                        if (dataSnapshot.child("text").getValue() != null)
                            text = dataSnapshot.child("text").getValue().toString();
                        if (dataSnapshot.child("creator").getValue() != null)
                            creatorID = dataSnapshot.child("creator").getValue().toString();
                        if (dataSnapshot.child("timestamp").getValue() != null)
                            timestamp = dataSnapshot.child("timestamp").getValue().toString();
                        byte[] encryptedText = new byte[text.length()/2];
                        for (int i = 0; i < encryptedText.length; i++) {
                            int index = i * 2;
                            int j = Integer.parseInt(text.substring(index, index + 2), 16);
                            encryptedText[i] = (byte) j;
                        }
                        String pText = decryptWithPrefixIV(encryptedText, secretKey);
                        MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, pText, timestamp);
                        //                        MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, text);
                        messageList.add(mMessage);
                        mChatLayoutManager.scrollToPosition(messageList.size() - 1);
                        mChatAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {  }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void sendMessage() throws Exception{
        EditText mMessage=findViewById(R.id.text);
        byte[] iv = CryptoUtils.getRandomNonce(IV_LENGTH_BYTE);

        if(!mMessage.getText().toString().isEmpty()){
            DatabaseReference newMessageDb= FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).push();
            Map newMessageMap=new HashMap<>();
            String pText=mMessage.getText().toString();
            byte[] encryptedText = encryptWithPrefixIV(pText.getBytes(UTF_8), secretKey, iv);
            newMessageMap.put("text",CryptoUtils.hex(encryptedText));

            //newMessageMap.put("text",mMessage.getText().toString() );
            newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
            String timestamp=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            String year=timestamp.substring(0,4);
            String month=timestamp.substring(5,7);
            String date=timestamp.substring(8,10);
            String hour=timestamp.substring(11,13);
            String minute=timestamp.substring(14,16);
            String timeDisplayed=date+"/"+month+"/"+year+" at "+hour+":"+minute;
            newMessageMap.put("timestamp",timeDisplayed);
            newMessageDb.updateChildren(newMessageMap);
        }
        mMessage.setText(null);
    }

    private void initializeRecyclerView(){
        byte[] encoded = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 0, 3, -105, 119, -53, -1, 96, 18, 7, -33, 122, -128, 0, 38, -2, 20, -22, 92, -111, 6, 81};
        secretKey = new SecretKeySpec(encoded, "AES");
        messageList=new ArrayList<>();
        mChat=findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled (false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager=new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL,false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter=new MessageAdapter(messageList,mName);
        mChat.setAdapter(mChatAdapter);
    }
}