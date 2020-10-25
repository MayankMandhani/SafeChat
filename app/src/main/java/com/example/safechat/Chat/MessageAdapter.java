package com.example.safechat.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safechat.R;
import com.example.safechat.User.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    ArrayList<MessageObject> messageList;
    String mName;
    public MessageAdapter(ArrayList<MessageObject> messageList, String mName){
        this.messageList=messageList;
        this.mName=mName;
    }

    @Override
    public int getItemViewType(int position) {
        MessageObject message = (MessageObject) messageList.get(position);

        if (message.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_MESSAGE_RECEIVED) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, null, false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);

            MessageViewHolder rcv = new MessageViewHolder(layoutView);
            return rcv;
        }
        else if(viewType==VIEW_TYPE_MESSAGE_SENT){
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sentmessageitem, null, false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);

            SentMessageViewHolder rcv = new SentMessageViewHolder(layoutView);
            return rcv;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        MessageObject message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageViewHolder) holder).bind(message,position);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((MessageViewHolder) holder).bind(message,position);
        }
    }

    @Override

    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView mMessage;
        RelativeLayout mLayout;
        TextView mTimeStamp;
        TextView mDate;
        MessageViewHolder(View view){
            super(view);
            mMessage=view.findViewById(R.id.message);
            mLayout=view.findViewById(R.id.layout);
            mTimeStamp=view.findViewById(R.id.timestamp);
            mDate=view.findViewById(R.id.date);
        }
        void bind(MessageObject message,int position) {
            mMessage.setText(message.getMessage());
            mTimeStamp.setText(message.getTimestamp().substring(14));
            String curDate=message.getTimestamp().substring(0,10);
            String lastMsgDate="";
            if(position>0) {
                lastMsgDate = messageList.get(position - 1).getTimestamp().substring(0, 10);
            }
            if(position==0||!curDate.equals(lastMsgDate)){
                mDate.setVisibility(View.VISIBLE);
                String year = message.getTimestamp().substring(6, 10);
                String month = message.getTimestamp().substring(3, 5);
                String date = message.getTimestamp().substring(0, 2);
                String displayDate=date+"/"+month+"/"+year;
                mDate.setText(displayDate);
            }
        }
    }

    public class SentMessageViewHolder extends RecyclerView.ViewHolder{
        TextView mMessage;
        RelativeLayout mLayout;
        TextView mTimeStamp;
        TextView mDate;
        SentMessageViewHolder(View view){
            super(view);
            mMessage=view.findViewById(R.id.message);
            mLayout=view.findViewById(R.id.layout);
            mTimeStamp=view.findViewById(R.id.timestamp);
            mDate=view.findViewById(R.id.date);
        }
        void bind(MessageObject message,int position) {
            mMessage.setText(message.getMessage());
            mTimeStamp.setText(message.getTimestamp().substring(14));
            String curDate=message.getTimestamp().substring(0,10);
            String lastMsgDate="";
            if(position>0) {
                lastMsgDate = messageList.get(position - 1).getTimestamp().substring(0, 10);
            }
            if (position==0||!curDate.equals(lastMsgDate)) {
                mDate.setVisibility(View.VISIBLE);
                String year = message.getTimestamp().substring(6, 10);
                String month = message.getTimestamp().substring(3, 5);
                String date = message.getTimestamp().substring(0, 2);
                String displayDate = date + "/" + month + "/" + year;
                mDate.setText(displayDate);
            }
            else{
                mDate.setVisibility(View.GONE);
            }
        }
    }
}
