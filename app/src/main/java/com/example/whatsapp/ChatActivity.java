package com.example.whatsapp;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID,msgID, prevSenderID, cat, msg,lat,log,retrieve = "";

    private TextView userName, userLastSeen,RetrieveView,ForwardView;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, BlockedMessageRef, BlockedMessageKeyRef;

    private ImageButton SendMessageButton, SendFilesButton,MessageInputVoice;
    private Button UserDetailsBtn;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;

    private static final String TAG = "TextClassificationDemo";
    private TextClassificationClient client;
    private String result1, resultText, predictResult;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String Keeper = "";
    private ArrayList<String> stopwords = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.v(TAG, "onCreate");
        client = new TextClassificationClient(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        BlockedMessageRef = FirebaseDatabase.getInstance().getReference().child("BlockedMessages").child("PrivateChats");

        try {
            InitializeControllers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(getIntent().getStringExtra("from").equals("ForwardFragment"))
        {
            messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
            messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
            messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
            msgID = Objects.requireNonNull(getIntent().getExtras().get("MsgId")).toString();
            prevSenderID = Objects.requireNonNull(getIntent().getExtras().get("prevSenderID")).toString();
            msg = Objects.requireNonNull(getIntent().getExtras().get("msg")).toString();
            cat = Objects.requireNonNull(getIntent().getExtras().get("cat")).toString();
            Toast.makeText(this, msgID+"\n"+prevSenderID+"\n"+msg+"\n"+cat, Toast.LENGTH_SHORT).show();
            msg = msg.trim();
            MessageInputText.setText(msg);
            try {
                ForwardMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(getIntent().getStringExtra("from").equals("ChatsFragment"))
        {
            messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
            messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
            messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
        }

        else  if(getIntent().getStringExtra("from").equals("BlockedMessagesFragment")){
            messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
            messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
            messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

            SendMessageButton.setVisibility(View.INVISIBLE);
            SendFilesButton.setVisibility(View.INVISIBLE);
            MessageInputText.setVisibility(View.INVISIBLE);
            MessageInputVoice.setVisibility(View.INVISIBLE);
            UserDetailsBtn.setVisibility(View.VISIBLE);

            UserDetailsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(ChatActivity.this,ProfileActivity.class);
                    profileIntent.putExtra("messageReceiverID",messageReceiverID);
                    profileIntent.putExtra("from", "AdminActivity");
                    startActivity(profileIntent);

                }
            });
        }

        else  if(getIntent().getStringExtra("from").equals("ReportedMessagesFragment")){
            messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
            messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
            messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

            SendMessageButton.setVisibility(View.INVISIBLE);
            SendFilesButton.setVisibility(View.INVISIBLE);
            MessageInputText.setVisibility(View.INVISIBLE);
            MessageInputVoice.setVisibility(View.INVISIBLE);
            UserDetailsBtn.setVisibility(View.VISIBLE);

            UserDetailsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(ChatActivity.this,ProfileActivity.class);
                    profileIntent.putExtra("messageReceiverID",messageReceiverID);
                    profileIntent.putExtra("from", "AdminActivity");
                    startActivity(profileIntent);

                }
            });
        }


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SendMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ChatActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results)
            {
                ArrayList<String> matchesFound = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                if (matchesFound != null)
                {
                    Keeper = matchesFound.get(0);
                    MessageInputText.setText(Keeper);
                    MessageInputVoice.setVisibility(View.INVISIBLE);
                    SendMessageButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });



        MessageInputVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        Keeper = "";
                        break;

                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        try {
                            SendMessage();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                }
                return false;
            }
        });


        DisplayLastSeen();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF Files",
                        "Audio Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select The File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);

                        }
                        if (i == 1) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);

                        }
                        if (i == 2) {
                            checker = "audio";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(intent.createChooser(intent, "Select Audio File"), 438);

                        }
                    }
                });
                builder.show();
            }
        });


        RootRef.child("Message").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        RootRef.child("BlockedMessages").child("PrivateChats").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        RootRef.child("ReportedMessages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.v(TAG, "onStart");
        client.load();

        MessageInputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                {
                    MessageInputVoice.setVisibility(View.INVISIBLE);
                    SendMessageButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
        client.unload();
    }

    private void InitializeControllers() throws IOException {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        RetrieveView = (TextView) findViewById(R.id.RetrieveView);
        ForwardView = (TextView) findViewById(R.id.ForwardView);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputVoice = (ImageButton) findViewById(R.id.voice_message_btn);
        UserDetailsBtn = (Button) findViewById(R.id.users_details_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        String line;

        try {

            InputStreamReader input;
            InputStream inputStream = getAssets().open("output.txt");
            input = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(input);

            while ((line = br.readLine()) != null) {
                stopwords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        getBlockKeywords();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, while we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            fileUri = data.getData();

            if(!checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                uploadTask = filePath.putFile(fileUri);

                storageReference.child("pdf").child(System.currentTimeMillis()+"").putFile(fileUri)

                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override

                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();

                                while (!uri.isComplete()) ;

                                Uri url = uri.getResult();

                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", url.toString());
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("category","direct");


                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);


                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading....");
                    }
                });

            }

            else if (checker.equals("image")){
                detectTxt();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            if (predictResult.equalsIgnoreCase("The message cannot be transmitted.")){
                                Uri downloadUrl = task.getResult();
                                myUrl = downloadUrl.toString();

                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", "https://firebasestorage.googleapis.com/v0/b/whatsapp-32a63.appspot.com/o/Image%20Files%2Fhqdefault.jpg?alt=media&token=43659ed3-ae5a-4eb9-aee1-b92ef2a562d8");
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("category","direct");


                                BlockedMessageKeyRef = BlockedMessageRef.child("liZlAZoGZ4dWQ3ripkMVZxiY0uB2").child(messageSenderID).child(messagePushID);
                                HashMap<String, Object> blockedMessageInfoMap = new HashMap<>();
                                blockedMessageInfoMap.put("from",messageSenderID);
                                blockedMessageInfoMap.put("to",messageReceiverID);
                                blockedMessageInfoMap.put("message",myUrl);
                                blockedMessageInfoMap.put("messageID",messagePushID);
                                blockedMessageInfoMap.put("date",saveCurrentDate);
                                blockedMessageInfoMap.put("time",saveCurrentTime);
                                blockedMessageInfoMap.put("type",checker);
                                BlockedMessageKeyRef.updateChildren(blockedMessageInfoMap);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){

                                            loadingBar.dismiss();
                                            Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        else {

                                            loadingBar.dismiss();
                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                        MessageInputText.setText("");
                                    }
                                });
                            }
                            else {
                                Uri downloadUrl = task.getResult();
                                myUrl = downloadUrl.toString();

                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", myUrl);
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("category","direct");


                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {

                                            loadingBar.dismiss();
                                            Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                        } else {

                                            loadingBar.dismiss();
                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                        MessageInputText.setText("");
                                    }
                                });
                            }
                        }
                    }
                });
            }
            else {

                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void DisplayLastSeen(){
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userState").hasChild("state")){
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")){
                        userLastSeen.setText("online");
                    }
                    else if (state.equals("offline")){
                        userLastSeen.setText("Last Seen: " + date + " " + time);

                    }
                }
                else {
                    userLastSeen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayTrack(String lat, String log) {
        try {
            Uri uri= Uri.parse(String.format("google.navigation:q=%s,%s",lat,log));
            Intent mapIntent=new Intent(Intent.ACTION_VIEW,uri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Uri uri =Uri.parse("https://play.google.com/store/apps/Detailed?id=com.google.android.apps.maps");
            Intent intent=new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    private void SendMessage() throws IOException {
        String messageText = MessageInputText.getText().toString();
        String result = classify(messageText);

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Message can not be empty..", Toast.LENGTH_SHORT).show();
        }
        else if (result.equalsIgnoreCase("The message cannot be transmitted.")){
            String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", "The message block due to disputing content");
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("category","direct");



            BlockedMessageKeyRef = BlockedMessageRef.child("liZlAZoGZ4dWQ3ripkMVZxiY0uB2").child(messageSenderID).child(messagePushID);
            HashMap<String, Object> blockedMessageInfoMap = new HashMap<>();
            blockedMessageInfoMap.put("condition","Blocked");
            blockedMessageInfoMap.put("from",messageSenderID);
            blockedMessageInfoMap.put("to",messageReceiverID);
            blockedMessageInfoMap.put("message",messageText);
            blockedMessageInfoMap.put("messageID",messagePushID);
            blockedMessageInfoMap.put("date",saveCurrentDate);
            blockedMessageInfoMap.put("time",saveCurrentTime);
            blockedMessageInfoMap.put("type","text");
            BlockedMessageKeyRef.updateChildren(blockedMessageInfoMap);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
        else {
            extractKeywords(messageText);
            String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("category","direct");


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
    }

    private void ForwardMessage() throws IOException {
        /**
         String msgText = MessageInputText.getText().toString();
         msgText = msgText.trim();
         String SentResult = classify(msgText);
         if (TextUtils.isEmpty(msgText)){
         Toast.makeText(this, "Message can not be empty..", Toast.LENGTH_SHORT).show();
         }
         else if (SentResult.equalsIgnoreCase("The message cannot be transmitted.")){
         String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
         String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

         DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();

         String messagePushID = userMessageKeyRef.getKey();

         Map messageTextBody = new HashMap();
         messageTextBody.put("message", "The message block due to disputing content");
         messageTextBody.put("type", "text");
         messageTextBody.put("from", messageSenderID);
         messageTextBody.put("to", messageReceiverID);
         messageTextBody.put("messageID", messagePushID);
         messageTextBody.put("time", saveCurrentTime);
         messageTextBody.put("date", saveCurrentDate);
         messageTextBody.put("category","forward");
         messageTextBody.put("initialSender",prevSenderID);



         BlockedMessageKeyRef = BlockedMessageRef.child("liZlAZoGZ4dWQ3ripkMVZxiY0uB2").child(messageSenderID).child(messagePushID);
         HashMap<String, Object> blockedMessageInfoMap = new HashMap<>();
         blockedMessageInfoMap.put("from",messageSenderID);
         blockedMessageInfoMap.put("to",messageReceiverID);
         blockedMessageInfoMap.put("message",msgText);
         blockedMessageInfoMap.put("messageID",messagePushID);
         blockedMessageInfoMap.put("date",saveCurrentDate);
         blockedMessageInfoMap.put("time",saveCurrentTime);
         blockedMessageInfoMap.put("type","text");
         blockedMessageInfoMap.put("category","forward");
         messageTextBody.put("initialSender",prevSenderID);
         BlockedMessageKeyRef.updateChildren(blockedMessageInfoMap);


         Map messageBodyDetails = new HashMap();
         messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
         messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

         RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()){
        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
        }
        else {
        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
        MessageInputText.setText("");
        }
        });
         }
         else {
         extractKeywords(msgText);
         String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
         String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

         DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();

         String messagePushID = userMessageKeyRef.getKey();

         Map messageTextBody = new HashMap();
         messageTextBody.put("message", msgText);
         messageTextBody.put("type", "text");
         messageTextBody.put("from", messageSenderID);
         messageTextBody.put("to", messageReceiverID);
         messageTextBody.put("messageID", messagePushID);
         messageTextBody.put("time", saveCurrentTime);
         messageTextBody.put("date", saveCurrentDate);
         messageTextBody.put("category","forward");
         messageTextBody.put("initialSender",prevSenderID);


         Map messageBodyDetails = new HashMap();
         messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
         messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

         RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()){
        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
        }
        else {
        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
        MessageInputText.setText("");
        }
        });
         }


         **/
        String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
        String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();

        String messagePushID = userMessageKeyRef.getKey();

        Map messageTextBody = new HashMap();
        messageTextBody.put("message", msg);
        messageTextBody.put("type", "text");
        messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("category","forward");
        messageTextBody.put("initialSender",prevSenderID);


        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
                MessageInputText.setText("");
            }
        });

    }

    /** Send input text to TextClassificationClient and get the classify messages. */
    private String classify(final String text) {

        String result;
        // Run text classification with TF Lite.
        List<TextClassificationClient.Result> results = client.classify(text);

        // Show classification result on screen
        result = showResult(text, results);
        return result.trim();
    }

    /** Show classification result on the screen.*/
    private String showResult(final String inputText, final List<TextClassificationClient.Result> results) {
        String textShow = "";
        String text = RetrieveView.getText().toString();
        String[] sensitivewords = text.split(", ");
        String[] inp = inputText.split(" ");
        int count=0;
        for (int o = 0; o < inp.length; o++) {
            for (int p = 0; p < sensitivewords.length; p++){
                if (inp[o].equalsIgnoreCase(sensitivewords[p]) && count != 2){
                    count = 1;
                    for (int i = 0; i < 1; i++) {
                        result1 = String.format(results.get(i).getTitle());
                    }
                    if(result1.equals("Negative")){
                        textShow = "The message cannot be transmitted.";
                        count++;
                        break;
                    }
                    else {
                        textShow = "The message is safe to transmit.";
                        break;
                    }
                }
            }
        }
        if (count == 0){
            textShow = "The message is safe to transmit.";
        }
        return textShow;

    }

    private void extractKeywords(String text) throws IOException {

        // Run text classification with TF Lite.
        List<TextClassificationClient.Result> results = client.classify(text);

        String resultSentiment = String.format(results.get(0).getTitle());
        if (resultSentiment.equals("Negative"))
        {
            try{
                String input_str = text.toLowerCase();
                input_str = input_str.replaceAll(Arrays.toString(new String[]{"\\p{Punct}", "+", "=", "*", "#", "$", "~", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"}), " ");

                ArrayList<String> allWords = Stream.of(input_str.split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));
                allWords.removeAll(stopwords);

                String result = String.join(",", allWords);
                String[] keywordsArray = result.split(",");

                if (keywordsArray.length != 0) {
                    Toast.makeText(this, String.valueOf(keywordsArray.length), Toast.LENGTH_SHORT).show();
                }
                for (int i = 0; i < keywordsArray.length; i++) {
                    if (!keywordsArray[i].equals("")) {
                        int j = i;
                        RootRef.child("Keywords").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(keywordsArray[j])) {
                                    int count = Integer.parseInt(dataSnapshot.child(keywordsArray[j]).child("count").getValue().toString());
                                    count = count + 1;
                                    if(count==10){
                                        RootRef.child("BlockList").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String mid = ", "+keywordsArray[j];
                                                String blockList = dataSnapshot.getValue().toString()+mid;
                                                RootRef.child("BlockList").setValue(blockList);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        RootRef.child("Keywords").child(keywordsArray[j]).child("count").setValue(count);
                                    }
                                    else {
                                        RootRef.child("Keywords").child(keywordsArray[j]).child("count").setValue(count);
                                    }

                                }
                                else {
                                    RootRef.child("Keywords").child(keywordsArray[j]).child("count").setValue(1);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }


                }
            }
            catch (Exception e) {

            }
        }

        /***     ArrayList<String> stopwords = new ArrayList<>();
         String line;

         try {

         InputStreamReader input;
         InputStream inputStream = getAssets().open("output.txt");
         input = new InputStreamReader(inputStream);
         BufferedReader br = new BufferedReader(input);

         while ((line = br.readLine()) != null) {
         stopwords.add(line);
         }

         String input_str = text.toLowerCase();
         input_str = input_str.replaceAll(Arrays.toString(new String[]{"\\p{Punct}", "+", "=", "*", "#", "$", "~", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"}), " ");

         ArrayList<String> allWords = Stream.of(input_str.split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));
         allWords.removeAll(stopwords);

         String result = String.join(",", allWords);
         String[] keywordsArray = result.split(",");

         if (keywordsArray.length != 0) {
         Toast.makeText(this, String.valueOf(keywordsArray.length), Toast.LENGTH_SHORT).show();
         }
         for (int i = 0; i < keywordsArray.length; i++) {
         if (!keywordsArray[i].equals("")) {
         int j = i;
         RootRef.child("Keywords").addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild(keywordsArray[j])) {
        int count = Integer.parseInt(dataSnapshot.child(keywordsArray[j]).child("count").getValue().toString());
        count = count + 1;
        if(count==10){
        RootRef.child("BlockList").addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        String mid = ", "+keywordsArray[j];
        String blockList = dataSnapshot.getValue().toString()+mid;
        RootRef.child("BlockList").setValue(blockList);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
        });
        RootRef.child("Keywords").child(keywordsArray[j]).child("count").setValue(count);
        }
        else {
        RootRef.child("Keywords").child(keywordsArray[j]).child("count").setValue(count);
        }

        }
        else {
        RootRef.child("Keywords").child(keywordsArray[j]).child("count").setValue(1);
        }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
        });

         }


         }
         }
         catch (Exception e) {

         }***/
    }



    private void getBlockKeywords(){
        RootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("BlockList")){
                    retrieve = (Objects.requireNonNull(dataSnapshot.child("BlockList").getValue()).toString());

                    RetrieveView.setText(retrieve);
                    RetrieveView.setVisibility(View.INVISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void detectTxt(){
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(ChatActivity.this,fileUri);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            detector.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            processTxt(firebaseVisionText);
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                }
                            });
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this,"Capture Image",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void processTxt(FirebaseVisionText text) {
        resultText = text.getText();
        if (resultText.matches("")) {
            Toast.makeText(ChatActivity.this, "No Text", Toast.LENGTH_SHORT).show();
            predictResult = "The message is safe to transmit.";

        }
        else {
            predictResult = classify(resultText);
        }
    }

}
