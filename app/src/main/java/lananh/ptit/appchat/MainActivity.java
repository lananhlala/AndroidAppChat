package lananh.ptit.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    GoogleSignInClient mGoogleSignInClient;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;
    private ImageView ivStart;
    private Button btChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivStart = findViewById(R.id.ivStart);
        btChat = findViewById(R.id.btChat);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mToolbar = findViewById(R.id.main_page_toolbar);
//        setSupportActionBar(mToolbar);
        //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mCurrentUser == null){
            sendToStart();
        }
        else {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
            String name = mCurrentUser.getDisplayName();
            getSupportActionBar().setTitle("Xin chào " + name);
            // tabs
//            mViewPager = (ViewPager) findViewById(R.id.tabPager);
//            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//            mViewPager.setAdapter(mSectionsPagerAdapter);
//
//            mTabLayout.setupWithViewPager(mViewPager);
//            mTabLayout.addTab(mTabLayout.newTab().setText("Danh sách bạn bè"));
        }
        btChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ListFriendActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
        if(currentUser == null){
            sendToStart();
        }else {
            mUserDatabase.child("online").setValue("true");
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
//            mUserDatabase.child("lastseen").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_log_out){
            mGoogleSignInClient.signOut();
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();

            sendToStart();
        }
        else if(item.getItemId()==R.id.main_account){
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.main_all_users){
            Intent intent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(intent);
        }
        return true;
    }
}