package lananh.ptit.appchat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private RecyclerView mRvRecyclerView;
    private String mCurrentUserId;
    private DatabaseReference mFriendDatabase, mUsersDatabase;
    private View mView;
    private FirebaseAuth mAuth;
    private String tmp;
    private Friend f;
    public FriendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment FriendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendFragment newInstance(String param1) {
        FriendFragment fragment = new FriendFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_friend, container, false);
        mRvRecyclerView = mView.findViewById(R.id.list_friends);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend").child(mCurrentUserId);
        mFriendDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mRvRecyclerView.setHasFixedSize(true);
        mRvRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(mFriendDatabase, Friend.class).build();
        final FirebaseRecyclerAdapter<Friend, FriendViewHolder> adapter = new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final FriendViewHolder friendViewHolder, int i, final Friend friend) {
                f = friend;
                tmp = friend.getDate();
                friendViewHolder.tvStatus.setText(tmp);
                final String list_user_id = getRef(i).getKey();
                DatabaseReference tm = mUsersDatabase.child(list_user_id);
                tm.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String name = dataSnapshot.child("name").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();
//                            String date = friend.getDate();
                            friendViewHolder.tvName.setText(name);
//                            friendViewHolder.tvStatus.setText(date);
                            Picasso.get().load(image).into(friendViewHolder.ivImage);
                        } else {
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);
                FriendViewHolder viewHolder = new FriendViewHolder(view);
                return viewHolder;
            }
        };
        mRvRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    public static class FriendViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvStatus;
        CircleImageView ivImage;

        public FriendViewHolder(@NonNull View itemView)
        {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
           ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}