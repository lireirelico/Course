package com.example.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class UsersActivity extends AppCompatActivity {

    private LinearLayoutManager usersLinearLayoutManager;
    private DatabaseReference usersDatabase;
    private FirebaseRecyclerAdapter<Users, UsersViewHolder> mFirebaseAdapter;
    private RecyclerView usersList;
    private RecyclerView usersListFull;
    private String UID;
    private int mLevel;
    private boolean focusFlag = true;
    int userCounter;
    int counter;
    private ProgressBar userProgressBar;
    private TextView userLabel;
    private ArrayList<Users> usersArrayList = new ArrayList<>();
    private UserRecyclerAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        final SearchView searchView = (SearchView) findViewById(R.id.searchUser);
        userLabel = findViewById(R.id.userLabel);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (focusFlag) {
                    userLabel.setVisibility(View.GONE);
                    focusFlag = false;
                }
                else {
                    focusFlag = true;
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                userLabel.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = usersList.getLayoutParams();

                params.height = 0;
                params.width = 0;
                usersListFull.setVisibility(View.GONE);
                usersListFull.setLayoutParams(params);

                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                usersList.setVisibility(View.VISIBLE);
                usersList.setLayoutParams(params);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ViewGroup.LayoutParams params = usersList.getLayoutParams();
                params.height = 0;
                params.width = 0;
                usersList.setVisibility(View.GONE);
                usersList.setLayoutParams(params);

                searchUser(newText);

                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                usersListFull.setVisibility(View.VISIBLE);
                usersListFull.setLayoutParams(params);

                return false;
            }
        });


        userProgressBar = findViewById(R.id.userProgressBar);
        userProgressBar.setVisibility(View.GONE);


        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");
        mLevel = intent.getIntExtra("mLevel", 0);

        Objects.requireNonNull(getSupportActionBar()).hide();

        //Рукуслер
        usersLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        //Подключаем базу данных
        usersDatabase = FirebaseDatabase.getInstance().getReference();

        SnapshotParser<Users> parser = new SnapshotParser<Users>() {
            @NonNull
            @Override
            public Users parseSnapshot(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                if (users != null) {
                    users.setId(dataSnapshot.getKey());
                }
                return users;
            }
        };

        //Откуда достаем наши чаты
        final DatabaseReference usersRef = usersDatabase.child(DatabasePath.USERS_PATH);

        //Инициализация списка
        usersList = findViewById(R.id.userRecyclerView);
        usersList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        usersListFull = findViewById(R.id.userSearchRecyclerView);

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(usersRef, parser)
                        .build();

        usersArrayList.clear();

        Query query = usersDatabase.child(DatabasePath.USERS_PATH);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    Users tempUser = user.getValue(Users.class);
                    if (!UID.equals(tempUser.getUserId()))
                        usersArrayList.add(tempUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter = new UserRecyclerAdapter(usersArrayList, mLevel, UID);

        usersListFull.setAdapter(adapter);
        usersListFull.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, final int position, @NonNull Users model) {

                //usersList.setVisibility(View.INVISIBLE);
               // userProgressBar.setVisibility(View.VISIBLE);

                userCounter++;

                //Обработка нажатия на работягу
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Users clickUser = getItem(position);
                        Toast toast = Toast.makeText(getApplicationContext(), clickUser.getId(), Toast.LENGTH_SHORT);
                        toast.show();

                        //Передача данных в активности профиля
                        Intent intent = new Intent(UsersActivity.this, UserProfileActivity.class);
                        intent.putExtra("userID", clickUser.getId());
                        startActivity(intent);
                    }
                });

                holder.createUserToDoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Users clickUser = getItem(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("UID", clickUser.getId());

                        ExampleCreateToDo exampleCreateToDo = new ExampleCreateToDo();
                        exampleCreateToDo.setArguments(bundle);
                        exampleCreateToDo.show(getSupportFragmentManager(), "example dialog");
                    }
                });

                Query employee = usersDatabase.child(DatabasePath.POSITION_PATH).orderByKey().equalTo(model.getEmployee());
                employee.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        counter++;
                        for (DataSnapshot employeeSnapshot : dataSnapshot.getChildren()) {

                            Position employeePosition = employeeSnapshot.getValue(Position.class);
                            if (employeePosition != null) {
                                holder.userEmployeeTextView.setText(employeePosition.getName());
                                int level = employeePosition.getLevel();

                                if (level < mLevel) {
                                    holder.createUserToDoButton.setVisibility(View.VISIBLE);
                                } else {
                                    holder.createUserToDoButton.setVisibility(View.GONE);
                                }
                            }
                        }
                        if (userCounter == counter) {
                          //  userProgressBar.setVisibility(View.GONE);
                           // usersList.setVisibility(View.VISIBLE);
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if (UID.equals(model.getId())){
                    holder.userItemLinearLayout.setVisibility(View.GONE);
                    holder.userItemLinearLayout.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                } else {
                    holder.userItemLinearLayout.setVisibility(View.VISIBLE);
                    holder.userItemLinearLayout.setLayoutParams(new RecyclerView
                            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    holder.userTextView.setText(model.getUserName());

                    if (model.getPhoto() != null) {
                        Glide.with(UsersActivity.this)
                                .load(model.getPhoto())
                                .into( holder.userImageView);
                    }
                    else holder.userImageView.setImageDrawable(ContextCompat
                            .getDrawable(UsersActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                }
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new UsersViewHolder(inflater.inflate(R.layout.item_user, viewGroup, false));
            }


        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = usersLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    usersList.scrollToPosition(itemCount); //ЕСЛИ ПОЯВИТСЯ БАГ С СПИСКОМ ТО ОН ТУТ!
                }
            }
        });

        usersList.setHasFixedSize(false);
        usersList.setLayoutManager(usersLinearLayoutManager);
        usersList.setAdapter(mFirebaseAdapter);

    }



    public void clickBackUserButton(View view) {
        finish();
    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView;
        ImageView userImageView;
        TextView userEmployeeTextView;
        RelativeLayout userItemLinearLayout;
        ImageButton createUserToDoButton;

        UsersViewHolder(View v) {
            super(v);
            userTextView = itemView.findViewById(R.id.userTextView);
            userImageView = itemView.findViewById(R.id.userImageView);
            userEmployeeTextView = itemView.findViewById(R.id.userEmployeeTextView);
            userItemLinearLayout = itemView.findViewById(R.id.userItemLinearLayout);
            createUserToDoButton = itemView.findViewById(R.id.createUserToDoButton);
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

    private void searchUser(String searchUser) {
        adapter.getFilter().filter(searchUser);
    }
}
