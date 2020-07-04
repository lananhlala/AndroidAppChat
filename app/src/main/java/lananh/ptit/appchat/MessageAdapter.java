package lananh.ptit.appchat;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Messages c = mMessageList.get(position);
        String fromUser = c.getFrom();
        String messageType = c.getType();
        final String mCurrentUser = mAuth.getCurrentUser().getUid();
        DatabaseReference user = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUser);
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
//                String time = dataSnapshot.child("time").getValue().toString();
                holder.tvName.setText(name);
                Picasso.get().load(image).into(holder.ivImage);
                holder.tvTime.setText(DateFormat.getDateTimeInstance().format(new Date()).toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (messageType.equals("text")){
            holder.mesImage.setVisibility(View.INVISIBLE);
            if (fromUser.equals(mCurrentUser)) {

                holder.tvMess.setBackgroundResource(R.drawable.message_text_background);
                holder.tvMess.setTextColor(Color.WHITE);
                holder.tvMess.setText(c.getMessage());
            }
            else {
                holder.tvMess.setBackgroundColor(Color.WHITE);
                holder.tvMess.setTextColor(Color.BLACK);
                holder.tvMess.setText(c.getMessage());
            }
        }
        else if(messageType.equals("image")){
            holder.tvMess.setVisibility(View.INVISIBLE);
            holder.mesImage.setVisibility(View.VISIBLE);
            Picasso.get().load(c.getMessage()).into(holder.mesImage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView tvMess,tvTime,tvName;
        public CircleImageView ivImage;
        public ImageView mesImage;
        public MessageViewHolder(View v) {
            super(v);
            tvMess = v.findViewById(R.id.tvMess);
            ivImage = v.findViewById(R.id.ivImage);
            tvTime = v.findViewById(R.id.tvTime);
            tvName = v.findViewById(R.id.tvName);
            mesImage = v.findViewById(R.id.ivMessImage);
        }
    }
}
