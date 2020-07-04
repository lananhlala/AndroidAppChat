package lananh.ptit.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListFriendActivity extends AppCompatActivity {
    private RecyclerView mRvRecyclerView;
    private String mCurrentUserId;
    private DatabaseReference mFriendDatabase, mUsersDatabase;
    private View mView;
    private FirebaseAuth mAuth;
    private String tmp;
    private Friend f;
    private FriendAdapter friendAdapter;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend);
        mToolbar = findViewById(R.id.friend_appBar);
        getSupportActionBar().setTitle("Bạn bè");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRvRecyclerView = findViewById(R.id.rv_friend);
        mRvRecyclerView.setHasFixedSize(true);
        mRvRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Friend").child(mCurrentUserId),Friend.class).build();
        friendAdapter = new ListFriendActivity.FriendAdapter(options);
        mRvRecyclerView.setAdapter(friendAdapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        friendAdapter.startListening();
    }

    @Override
    protected void onStop() {

        super.onStop();
        friendAdapter.stopListening();
    }
    public class FriendAdapter extends FirebaseRecyclerAdapter<Friend, ListFriendActivity.FriendAdapter.FriendViewHolder> {

        public FriendAdapter(@NonNull FirebaseRecyclerOptions<Friend> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(final FriendViewHolder userViewHolder, int i, final Friend user) {
            userViewHolder.tvStatus.setText(user.getDate());
            final String user_id = getRef(i).getKey();
            DatabaseReference tm = mUsersDatabase.child(user_id);
            tm.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();
                        userViewHolder.tvName.setText(name);
                        Picasso.get().load(image).into(userViewHolder.ivImage);
                        if(dataSnapshot.hasChild("online")){
                            String online = dataSnapshot.child("online").getValue().toString();
                            if(online.equals("true")){
                                userViewHolder.ivOnline.setVisibility(View.VISIBLE);
                            }
                            else {
                                userViewHolder.ivOnline.setVisibility(View.INVISIBLE);
                            }
                        }

                    } else {
                        Toast.makeText(ListFriendActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            userViewHolder.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharSequence opts[] = new CharSequence[]{"Tới trang cá nhân","Chat ngay"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ListFriendActivity.this);
                    builder.setTitle("Lựa chọn");
                    builder.setItems(opts, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // click event for each item
                            if(which==0){
                                Intent intent = new Intent(ListFriendActivity.this,ProfileActivity.class);
                                intent.putExtra("user_id",user_id);
                                startActivity(intent);
                            }
                            if(which==1) {
                                Intent intent = new Intent(ListFriendActivity.this,ChatActivity.class);
                                intent.putExtra("user_id",user_id);
                                startActivity(intent);
                            }
                        }
                    });
                    builder.show();

                }
            });
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);
            return new FriendViewHolder(view);
        }

        class FriendViewHolder extends RecyclerView.ViewHolder{
            TextView tvName, tvStatus;
            CircleImageView ivImage;
            ImageView ivOnline;
            public FriendViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvTime);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                ivImage = itemView.findViewById(R.id.ivImage);
                ivOnline = itemView.findViewById(R.id.ivOnline);
            }
        }
    }

}