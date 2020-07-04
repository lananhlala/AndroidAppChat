package lananh.ptit.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private UserAdapter userAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolbar = findViewById(R.id.users_appBar);
        getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsersList = findViewById(R.id.rv_users);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Users"),User.class).build();
        userAdapter = new UserAdapter(options);
        mUsersList.setAdapter(userAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        userAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userAdapter.stopListening();
    }
    public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.UserViewHolder> {

        public UserAdapter(@NonNull FirebaseRecyclerOptions<User> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(UserViewHolder userViewHolder, int i, final User user) {
            userViewHolder.tvName.setText(user.getName());
            userViewHolder.tvStatus.setText(user.getStatus());
            Picasso.get().load(user.getImage()).into(userViewHolder.ivImage);
            final String user_id = getRef(i).getKey();
            userViewHolder.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UsersActivity.this,ProfileActivity.class);
                    intent.putExtra("user_id",user_id);
                    startActivity(intent);
                }
            });
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);
            return new UserViewHolder(view);
        }

        class UserViewHolder extends RecyclerView.ViewHolder{
            TextView tvName, tvStatus;
            CircleImageView ivImage;
            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                ivImage = itemView.findViewById(R.id.ivImage);
            }
        }
    }

}
