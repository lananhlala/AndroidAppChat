package lananh.ptit.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private ImageView ivCover;
    private TextView tvName, tvStatus, tvTotalFriend;
    private Button btMakeFriend,btDeclineFriend;
    private DatabaseReference mUserDatabase, mFriendRequestDatabase, mFriendDatabase, mNotificationDatabase;
    private String mCurrent_state;
    private FirebaseUser mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ivCover = findViewById(R.id.ivCover);
        tvName = findViewById(R.id.tvName);
        tvStatus = findViewById(R.id.tvStatus);
        btMakeFriend = findViewById(R.id.btMakeFriend);
        btDeclineFriend = findViewById(R.id.btDeclineFriend);
        final String user_id = getIntent().getStringExtra("user_id");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                tvName.setText(name);
                tvStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.jd_07_512).into(ivCover);
                // ------ FRIEND LIST/ REQUEST FEATURE ---------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrent_state = "req_received";
                                btMakeFriend.setText("Chấp nhận yêu cầu kết bạn");
                                btDeclineFriend.setVisibility(View.VISIBLE);
                            }
                            else if(req_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                btMakeFriend.setText("Hủy yêu cầu kết bạn");
                            }
                        }
                        else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friend";
                                        btMakeFriend.setVisibility(View.INVISIBLE);
                                        btDeclineFriend.setText("Hủy quan hệ bạn bè");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mCurrent_state = "not_friend";
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        btMakeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btMakeFriend.setEnabled(false);
                // -------- NOT FRIEND STATE -----------
                if(mCurrent_state.equals("not_friend")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String,String> notificationData = new HashMap<>();
                                        notificationData.put("from",mCurrentUser.getUid());
                                        notificationData.put("type","request");
                                        mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                btMakeFriend.setEnabled(true);
                                                mCurrent_state = "req_sent";
                                                btMakeFriend.setText("Hủy yêu cầu kết bạn");
                                                Toast.makeText(ProfileActivity.this, "Đã gửi yêu cầu kết bạn!",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                            else {
                                Toast.makeText(ProfileActivity.this,"Có lỗi xảy ra",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // ---------- CANCEL REQUEST STATE -------------
                if(mCurrent_state.equals("req_sent")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btMakeFriend.setEnabled(true);
                                    mCurrent_state = "not_friend";
                                    btMakeFriend.setText("Kết bạn");
                                }
                            });
                        }
                    });
                }

                // ------- REQUEST RECEIVED -------
                if(mCurrent_state.equals("req_received")){
                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    btMakeFriend.setVisibility(View.INVISIBLE);
                                                                    mCurrent_state = "friend";
                                                                    btDeclineFriend.setVisibility(View.VISIBLE);
                                                                    btDeclineFriend.setText("Hủy quan hệ bạn bè");
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                }
            }
        });

        btDeclineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btDeclineFriend.setEnabled(false);
                // ------------- Huy yeu cau ket ban -----------
                if(mCurrent_state.equals("req_received")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btMakeFriend.setEnabled(true);
                                    btMakeFriend.setText("Kết bạn");
                                    mCurrent_state = "not_friend";
                                    btDeclineFriend.setVisibility(View.INVISIBLE);
                                    Toast.makeText(ProfileActivity.this,"Từ chối yêu cầu kết bạn thành công",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }

                // -------- UNFRIEND --------
                if(mCurrent_state.equals("friend")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mCurrent_state = "not_friend";
                            btMakeFriend.setEnabled(true);
                            btMakeFriend.setText("Kết bạn");
                            btDeclineFriend.setVisibility(View.INVISIBLE);
                            Toast.makeText(ProfileActivity.this,"Hai bạn không còn là bạn bè",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}