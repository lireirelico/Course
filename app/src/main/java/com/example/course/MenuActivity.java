package com.example.course;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MenuActivity";

    private String CHATS_PATH = "Chats";
    private String UID;
    private String USERS_PATH = "Users";
    private DatabaseReference chatDatabase;
    private RecyclerView chatRecyclerView;
    private LinearLayoutManager chatLinearLayoutManager;
    private RecyclerView chatList;
    private FirebaseRecyclerAdapter<Chats, ChatViewHolder> mFirebaseAdapter;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private String mPhoto;
    private String mUsername;
    private Users user;
    private int mLevel;
    private ImageView mPhotoImageView;
    private TextView mNameTextView;
    private TextView mEmployeeTextView;



    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Новый диалог", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                openDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        mPhotoImageView = header.findViewById(R.id.mPhotoImageView);
        mNameTextView = header.findViewById(R.id.mNameTextView);
        mEmployeeTextView = header.findViewById(R.id.mEmployeeTextView);

        //Рукуслер
        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        chatLinearLayoutManager = new LinearLayoutManager(this);
        chatLinearLayoutManager.setReverseLayout(true);
        chatLinearLayoutManager.setStackFromEnd(true);

        //Подключаем базу данных
        chatDatabase = FirebaseDatabase.getInstance().getReference();

        SnapshotParser<Chats> parser = new SnapshotParser<Chats>() {
            @NonNull
            @Override
            public Chats parseSnapshot(DataSnapshot dataSnapshot) {
                Chats chats = dataSnapshot.getValue(Chats.class);
                if (chats != null) {
                    chats.setId(dataSnapshot.getKey());
                }
                return chats;
            }
        };

        UID = mFirebaseUser.getUid();

        //Откуда достаем наши чаты
        final DatabaseReference chatRef = chatDatabase.child(CHATS_PATH).child(UID);

        //Инициализация списка
        chatList = findViewById(R.id.chatRecyclerView);
        chatList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FirebaseRecyclerOptions<Chats> options =
                new FirebaseRecyclerOptions.Builder<Chats>()
                        .setQuery(chatRef, parser)
                        .build();

        Query userInformation = chatDatabase.child(DatabasePath.USERS_PATH).orderByKey().equalTo(UID);;
        userInformation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    user = userSnapshot.getValue(Users.class);

                    assert user != null;
                    if (user.getPhoto() != null) {
                        mNameTextView.setText(user.getUserName());
                        mPhoto = user.getPhoto();
                        Glide.with(MenuActivity.this)
                                .load(mPhoto)
                                .into(mPhotoImageView);
                    }
                    else
                    {
                        mNameTextView.setText(user.getUserName());
                        mPhotoImageView.setImageDrawable(ContextCompat.getDrawable(MenuActivity.this,
                                R.drawable.ic_account_circle_black_36dp));
                        mPhoto = null;
                    }

                    final Query mEmployee = chatDatabase.child(DatabasePath.POSITION_PATH).orderByKey().equalTo(user.getEmployee());
                    mEmployee.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot employeeSnapshot : dataSnapshot.getChildren()) {
                                Position mPosition = employeeSnapshot.getValue(Position.class);
                                mLevel = mPosition.getLevel();
                                mEmployeeTextView.setText(mPosition.getName());
                                if (mLevel == 80) navigationView.getMenu().findItem(R.id.nav_employee).setVisible(true);
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

        //Адаптер от гугла
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Chats, ChatViewHolder>(options) {

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new ChatViewHolder(inflater.inflate(R.layout.item_chat, viewGroup, false));
            }

            //Получаем данные по чатам
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder viewHolder,
                                            final int position,
                                            @NonNull Chats chats) {

                //Обработка нажатия на элемент
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Chats clickChat = getItem(position);
                        String dialogID = clickChat.getChatId();
                        String dialogName = viewHolder.chatDestination.getText().toString();

                        //Передача данных в активности диалога
                        Intent intent = new Intent(MenuActivity.this, DialogueActivity.class);
                        intent.putExtra("dialogID", dialogID);
                        intent.putExtra("Uid", UID);
                        intent.putExtra("mPhoto", mPhoto);
                        intent.putExtra("dialogName", dialogName);
                        startActivity(intent);
                    }
                });

                    if (null != chats.getDestination()) {
                        Query firebaseSearchQuery = chatDatabase.child(USERS_PATH).orderByKey().equalTo(chats.getDestination());

                        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                    Users user = userSnapshot.getValue(Users.class);
                                    String name = user.getUserName();
                                    if (user != null)
                                        viewHolder.chatDestination.setText(user.getUserName());

                                    if (user.getPhoto() != null) {
                                        Glide.with(MenuActivity.this)
                                                .load(user.getPhoto())
                                                .into(viewHolder.userPhotoChatItem);
                                    } else {
                                        viewHolder.userPhotoChatItem.setImageDrawable(ContextCompat.getDrawable(MenuActivity.this,
                                                R.drawable.ic_account_circle_black_36dp));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        viewHolder.chatDestination.setVisibility(TextView.VISIBLE);
                        }

                    if (chats.getLastMessage() != null) {
                        String lastMessage = chats.getLastMessage();
                        try {
                            lastMessage = MyChiper.decrypt(lastMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (lastMessage.length() > 24) {
                            String msg = "";
                            for (int i = 0; i < 24; i++)
                            {
                                msg += lastMessage.charAt(i);
                            }
                            msg += "...";

                            viewHolder.chatLastMessage.setText(msg);
                            viewHolder.chatLastMessage.setVisibility(TextView.VISIBLE);
                        } else {
                            viewHolder.chatLastMessage.setText(lastMessage);
                            viewHolder.chatLastMessage.setVisibility(TextView.VISIBLE);
                        }
                    }

            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = chatLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    chatList.scrollToPosition(positionStart);
                }
            }
        });

        chatList.setLayoutManager(chatLinearLayoutManager);
        chatList.setAdapter(mFirebaseAdapter);
    }

    private void openDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("UID", UID);

        ExampleCreateDialog exampleCreateDialog = new ExampleCreateDialog();
        exampleCreateDialog.setArguments(bundle);
        exampleCreateDialog.show(getSupportFragmentManager(), "example dialog");
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView chatDestination;
        TextView chatLastMessage;
        LinearLayout chatLinearLayout;
        ImageView userPhotoChatItem;

        ChatViewHolder(View v) {
            super(v);
            chatDestination = itemView.findViewById(R.id.chatDestinationTextView);
            chatLastMessage = itemView.findViewById(R.id.chatLastMessageTextView);
            chatLinearLayout = itemView.findViewById(R.id.chatLinearLayout);
            userPhotoChatItem = itemView.findViewById(R.id.userphotoItemChatImageView);
        }

    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MenuActivity.this, SettingActivity.class);
            intent.putExtra("UID", UID);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent intent = new Intent(MenuActivity.this, UsersActivity.class);
            intent.putExtra("UID", UID);
            intent.putExtra("mLevel", mLevel);
            startActivity(intent);

        } else if (id == R.id.nav_gallery) {
            //Передача данных в активности диалога
            Intent intent = new Intent(MenuActivity.this, ToDoActivity.class);
            intent.putExtra("Uid", UID);
            startActivity(intent);


        } else if (id == R.id.nav_send) {
            mFirebaseAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            mFirebaseUser = null;
            mUsername = ANONYMOUS;
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else if (id == R.id.nav_employee) {
            // Handle the camera action
            Intent intent = new Intent(MenuActivity.this, UserEmployeeRedactorActivity.class);
            intent.putExtra("UID", UID);
            intent.putExtra("mLevel", mLevel);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
