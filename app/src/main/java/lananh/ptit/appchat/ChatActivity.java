package lananh.ptit.appchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ContentFrameLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser,mChatUserName;
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private CircleImageView ivImage;
    private ImageButton btSendFile, btSendMess;
    private EditText etMess;
    private ImageView ivOnline;
    private FirebaseAuth mAuth;
    private String mCurrentUser;
    private RecyclerView mMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter messageAdapter;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatToolbar = findViewById(R.id.chat_app_bar);
        mImageStore = FirebaseStorage.getInstance().getReference();
//        setSupportActionBar(mChatToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        mChatUser = getIntent().getStringExtra("user_id");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        // ___custom action bar item _______
        ivOnline = findViewById(R.id.ivOnline);
        btSendFile = findViewById(R.id.btSendFile);
        btSendMess = findViewById(R.id.btSendMess);
        etMess = findViewById(R.id.etMess);
        ivImage = findViewById(R.id.ivImage);
        mMessageList = findViewById(R.id.rv_mess_list);
        messageAdapter = new MessageAdapter(messagesList);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayoutManager);
        mMessageList.setAdapter(messageAdapter);
        loadMess();
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatUserName = dataSnapshot.child("name").getValue().toString();
                String lastseen = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
//                mChatUserName = chatUserName;
                actionBar.setTitle(chatUserName);
                if(lastseen.equals("true")){
                    ivOnline.setVisibility(View.VISIBLE);
                }
                else{
                    ivOnline.setVisibility(View.INVISIBLE);
                }
                Picasso.get().load(image).into(ivImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mRootRef.child("Chat").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUser+"/"+mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUser,chatAddMap);
                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener(){

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        btSendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        btSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Chọn Hình Ảnh"),GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode== RESULT_OK){
            Uri imageUri = data.getData();
            final String currentUserRef = "messages/"+mCurrentUser+"/"+mChatUser;
            final String chatUserRef = "messages/"+mChatUser+"/"+mCurrentUser;
            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUser).child(mChatUser).push();
            final String push_id = user_message_push.getKey();
            StorageReference filepath = mImageStore.child("messageImages").child(push_id+".jpg");
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();
                            Map messageMap = new HashMap();
                            messageMap.put("message",downloadUrl);
                            messageMap.put("seen",false);
                            messageMap.put("type","image");
                            messageMap.put("time",DateFormat.getDateTimeInstance().format(new Date()));
                            messageMap.put("from",mCurrentUser);
                            Map messageUserMap = new HashMap();
                            messageUserMap.put(currentUserRef +"/"+push_id,messageMap);
                            messageUserMap.put(chatUserRef+"/"+push_id,messageMap);
                            etMess.setText("");
                            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError!=null){
                                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                    }
                                }
                            });
                        }
                    });

                }
            });
        }
    }

    private void loadMess() {
        DatabaseReference messRef = mRootRef.child("messages").child(mCurrentUser).child(mChatUser);
        messRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
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

    private void sendMessage() {
        String message = etMess.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/"+mCurrentUser+"/"+mChatUser;
            String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUser;
            DatabaseReference user_mess_push = mRootRef.child("messages").child(mCurrentUser).child(mChatUser).push();
            String push_id = user_mess_push.getKey();
            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time", DateFormat.getDateTimeInstance().format(new Date()));
            messageMap.put("from",mCurrentUser);
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref +"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);
            etMess.setText("");
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}