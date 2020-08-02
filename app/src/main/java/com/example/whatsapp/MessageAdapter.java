package com.example.whatsapp;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, Rootref;
    private String Id,msg,category,lat,log;

    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        Rootref = FirebaseDatabase.getInstance().getReference();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text")) {

            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            } else {

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());


            }

        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("audio")){
            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-ab088.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=063daf4b-78d0-4778-84d9-7a1fd937c715").into(messageViewHolder.messageSenderPicture);

            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-ab088.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=063daf4b-78d0-4778-84d9-7a1fd937c715").into(messageViewHolder.messageReceiverPicture);
            }
        }

        if (fromUserID.equals(messageSenderID)){
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("audio")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View this file",
                                "Cancel",
                                "Delete for Everyone",
                                "Forward"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 4){
                                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                    String Id = rootRef.child("Message").child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getMessageID()).toString();
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                    intent.putExtra("Id",Id);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Delete for Everyone",
                                "Forward"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 2){
                                    deleteMessageForEveryone(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==3){
                                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                    Id = userMessagesList.get(position).getMessageID();
                                    msg= userMessagesList.get(position).getMessage();
                                    category = userMessagesList.get(position).getCategory();
                                    if (category.equals("direct"))
                                    {
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                        intent.putExtra("messageSenderID",messageSenderID);
                                        intent.putExtra("Id",Id);
                                        intent.putExtra("msg", msg);
                                        intent.putExtra("category", category);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if (category.equals("forward"))
                                    {
                                        String initialSender = userMessagesList.get(position).getInitialSender();
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                        intent.putExtra("messageSenderID",initialSender);
                                        intent.putExtra("Id",Id);
                                        intent.putExtra("msg", msg);
                                        intent.putExtra("category", category);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }

                                }

                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel",
                                "Delete for Everyone",
                                "Forward"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                                else if (i == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i==4){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

        else if (messageSenderID.equals("liZlAZoGZ4dWQ3ripkMVZxiY0uB2")){
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessagesList.get(position).getCondition().equals("Blocked"))
                    {
                        if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("audio")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "Download and View this file",
                                    "Cancel",
                                    "Send Details to Cyber Crime"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0){
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1){
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });
                            builder.show();
                        }
                        else if (userMessagesList.get(position).getType().equals("text")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "Cancel",
                                    "Send Details to Cyber Crime"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0){
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }

                                }
                            });
                            builder.show();
                        }

                        else if (userMessagesList.get(position).getType().equals("image")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "View This Image",
                                    "Cancel",
                                    "Send details to Cyber Crime"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0){
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1){
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });
                            builder.show();
                        }
                    }
                    else if (userMessagesList.get(position).getCondition().equals("Reported"))
                    {
                        if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("audio")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "Download and View this file",
                                    "Cancel",
                                    "Send Details to Cyber Crime",
                                    "Sender Details"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0){
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1){
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });
                            builder.show();
                        }
                        else if (userMessagesList.get(position).getType().equals("text")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "Cancel",
                                    "Send Details to Cyber Crime",
                                    "Sender Details",
                                    "Initial Sender Details"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0){
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if (i == 3)
                                    {
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ProfileActivity.class);
                                        intent.putExtra("messageReceiverID",messages.getFrom());
                                        intent.putExtra("from", "AdminActivity");
                                        messageViewHolder.itemView.getContext().startActivity(intent);

                                    }
                                    else if (i == 4)
                                    {
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ProfileActivity.class);
                                        intent.putExtra("messageReceiverID",messages.getInitialSender());
                                        intent.putExtra("from", "AdminActivity");
                                        messageViewHolder.itemView.getContext().startActivity(intent);

                                    }


                                }
                            });
                            builder.show();
                        }

                        else if (userMessagesList.get(position).getType().equals("image")){
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "View This Image",
                                    "Cancel",
                                    "Send details to Cyber Crime",
                                    "Sender Details",
                                    "Initial Sender Details"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    if (i == 0){
                                        deleteReceiveMessage(position, messageViewHolder);
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(i == 1){
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });
                            builder.show();
                        }
                    }
                }
            });
        }


        else {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("audio")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View this file",
                                "Cancel",
                                "Forward"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteReceiveMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==3){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Forward",
                                "Report"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteReceiveMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i==2){
                                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                    Id = userMessagesList.get(position).getMessageID();
                                    msg= userMessagesList.get(position).getMessage();
                                    category = userMessagesList.get(position).getCategory();
                                    if (category.equals("direct"))
                                    {
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                        intent.putExtra("messageSenderID",messages.getFrom());
                                        intent.putExtra("Id",Id);
                                        intent.putExtra("msg", msg);
                                        intent.putExtra("category", category);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if (category.equals("forward"))
                                    {
                                        String initialSender = messages.getInitialSender();
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                        intent.putExtra("messageSenderID",initialSender);
                                        intent.putExtra("Id",Id);
                                        intent.putExtra("msg", msg);
                                        intent.putExtra("category", category);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                }

                                else if (i==3)
                                {

                                    if (messages.getCategory().equals("direct")){
                                        DatabaseReference ReportedMessageKeyRef = Rootref.child("ReportedMessages").child("liZlAZoGZ4dWQ3ripkMVZxiY0uB2").child(messageSenderID).child(messages.getMessageID());
                                        HashMap<String, Object> blockedMessageInfoMap = new HashMap<>();
                                        blockedMessageInfoMap.put("condition","Reported");
                                        blockedMessageInfoMap.put("initialSender",messages.getFrom());
                                        blockedMessageInfoMap.put("from",messages.getFrom());
                                        blockedMessageInfoMap.put("to",messages.getTo());
                                        blockedMessageInfoMap.put("message",messages.getMessage());
                                        blockedMessageInfoMap.put("messageID",messages.getMessageID());
                                        blockedMessageInfoMap.put("date",messages.getDate());
                                        blockedMessageInfoMap.put("time",messages.getTime());
                                        blockedMessageInfoMap.put("type",messages.getType());
                                        blockedMessageInfoMap.put("category",messages.getCategory());
                                        ReportedMessageKeyRef.updateChildren(blockedMessageInfoMap);
                                    }
                                    else if(messages.getCategory().equals("forward")){
                                        DatabaseReference ReportedMessageKeyRef = Rootref.child("ReportedMessages").child("liZlAZoGZ4dWQ3ripkMVZxiY0uB2").child(messageSenderID).child(messages.getMessageID());
                                        HashMap<String, Object> blockedMessageInfoMap = new HashMap<>();
                                        blockedMessageInfoMap.put("condition","Reported");
                                        blockedMessageInfoMap.put("initialSender",messages.getInitialSender());
                                        blockedMessageInfoMap.put("from",messages.getFrom());
                                        blockedMessageInfoMap.put("to",messages.getTo());
                                        blockedMessageInfoMap.put("message",messages.getMessage());
                                        blockedMessageInfoMap.put("messageID",messages.getMessageID());
                                        blockedMessageInfoMap.put("date",messages.getDate());
                                        blockedMessageInfoMap.put("time",messages.getTime());
                                        blockedMessageInfoMap.put("type",messages.getType());
                                        blockedMessageInfoMap.put("category",messages.getCategory());
                                        ReportedMessageKeyRef.updateChildren(blockedMessageInfoMap);
                                    }

                                }

                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel",
                                "Forward"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteReceiveMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==3){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ForwardActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }



    private void deleteSentMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }



    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    rootRef.child("Message").child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


}

