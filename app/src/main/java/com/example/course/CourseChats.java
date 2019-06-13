package com.example.course;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CourseChats extends AppCompatActivity {

    private String CHATS_PATH = "Chats";
    private String USERS_PATH = "Users";
    private DatabaseReference chatDatabase;
    private RecyclerView chatRecyclerView;
    private LinearLayoutManager chatLinearLayoutManager;
    private RecyclerView chatList;
    private FirebaseRecyclerAdapter<Chats, ChatViewHolder> mFirebaseAdapter;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_chats);

        //Рукуслер
        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        chatLinearLayoutManager = new LinearLayoutManager(this);

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

        //Откуда достаем наши чаты
        final DatabaseReference chatRef = chatDatabase.child(CHATS_PATH);
        final DatabaseReference userRef = chatDatabase.child(USERS_PATH);

        //Инициализация списка
        chatList = findViewById(R.id.chatRecyclerView);
        chatList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FirebaseRecyclerOptions<Chats> options =
                new FirebaseRecyclerOptions.Builder<Chats>()
                        .setQuery(chatRef, parser)
                        .build();

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
                        Toast toast = Toast.makeText(getApplicationContext(), clickChat.getId(), Toast.LENGTH_SHORT);
                        toast.show();
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
                                Toast toast = Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    viewHolder.chatDestination.setVisibility(TextView.VISIBLE);
                }

                if (chats.getLastMessage() != null) {
                    viewHolder.chatLastMessage.setText(chats.getLastMessage());
                    viewHolder.chatLastMessage.setVisibility(TextView.VISIBLE);
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


    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView chatDestination;
        TextView chatLastMessage;
        LinearLayout chatLinearLayout;

        ChatViewHolder(View v) {
            super(v);
            chatDestination = itemView.findViewById(R.id.chatDestinationTextView);
            chatLastMessage = itemView.findViewById(R.id.chatLastMessageTextView);
            chatLinearLayout = itemView.findViewById(R.id.chatLinearLayout);
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

}
