package com.example.course;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
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

import java.util.Objects;

public class ToDoActivity extends AppCompatActivity {

    private LinearLayoutManager todoLinearLayoutManager;
    private DatabaseReference todoDatabase;
    private FirebaseRecyclerAdapter<ToDo, todoViewHolder> mFirebaseAdapter;
    private RecyclerView todoList;
    private static String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        Intent intent = getIntent();
        userID = intent.getStringExtra("Uid");

        final Toolbar toolbar = (Toolbar) findViewById(R.id.todoToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.todoSpinner);
        spinner.setAdapter(new MyAdapter(
                toolbar.getContext(),
                new String[]{
                        "Задачи от начальство",
                        "Личные задачи",
                }));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.todoContainer, PlaceholderFragment.newInstance(position + 1))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.todoFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                openDialog();
            }
        });
    }

    public void clickBackToDoButton(View view) {
        finish();
    }

    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    @SuppressLint("ValidFragment")
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private LinearLayoutManager todoLinearLayoutManager;
        private DatabaseReference todoDatabase;
        private FirebaseRecyclerAdapter<ToDo, todoViewHolder> mFirebaseAdapter;
        private RecyclerView todoList;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_to_do, container, false);

            final int section = getArguments().getInt(ARG_SECTION_NUMBER);
            //Работа с базой данных
            //Рукуслер
            todoLinearLayoutManager = new LinearLayoutManager(rootView.getContext());

            //Подключаем базу данных
            todoDatabase = FirebaseDatabase.getInstance().getReference();

            SnapshotParser<ToDo> parser = new SnapshotParser<ToDo>() {
                @NonNull
                @Override
                public ToDo parseSnapshot(DataSnapshot dataSnapshot) {
                    ToDo toDo = dataSnapshot.getValue(ToDo.class);
                    if (toDo != null) {
                        toDo.setId(dataSnapshot.getKey());
                    }
                    return toDo;
                }
            };

            //Откуда достаем наши чаты
            final DatabaseReference todoRef = todoDatabase.child(DatabasePath.TODO_PATH).child(userID);

            //Инициализация списка
            todoList = rootView.findViewById(R.id.todoRecyclerView);
            todoList.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext()));

            if (section == 1) {
                FirebaseRecyclerOptions<ToDo> options =
                        new FirebaseRecyclerOptions.Builder<ToDo>()
                                .setQuery(todoRef.orderByChild("me").equalTo(false), parser)
                                .build();

                mFirebaseAdapter = new FirebaseRecyclerAdapter<ToDo, todoViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final todoViewHolder holder, int position, @NonNull ToDo model) {
                        holder.senderNameToDo.setText(model.getSender());
                        holder.senderNameToDo.setVisibility(View.VISIBLE);
                        holder.titleToDo.setText(model.getTitle());
                        holder.descriptionToDo.setText(model.getDescription());
                        holder.todoID.setText(model.getId());

                        holder.checkToDoButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Завершение задачи
                                String deleteItem = holder.todoID.getText().toString();
                                Query deleteToDo = todoDatabase.child(DatabasePath.TODO_PATH).child(userID).orderByKey().equalTo(deleteItem);
                                deleteToDo.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot todoSnapshot : dataSnapshot.getChildren()) {
                                            todoSnapshot.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                Toast toast = Toast.makeText(getContext(), section + " click" + holder.todoID.getText(), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public todoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                        return new todoViewHolder(inflater.inflate(R.layout.item_todo, viewGroup, false));
                    }
                };
            } else if (section == 2) {
                FirebaseRecyclerOptions<ToDo> options =
                        new FirebaseRecyclerOptions.Builder<ToDo>()
                                .setQuery(todoRef.orderByChild("me").equalTo(true), parser)
                                .build();

                mFirebaseAdapter = new FirebaseRecyclerAdapter<ToDo, todoViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final todoViewHolder holder, int position, @NonNull ToDo model) {
                        if (userID.equals(model.getSender())) holder.senderNameToDo.setText("Меня");
                        else holder.senderNameToDo.setText(model.getSender());

                        holder.senderNameToDo.setVisibility(View.VISIBLE);
                        holder.titleToDo.setText(model.getTitle());
                        holder.descriptionToDo.setText(model.getDescription());
                        holder.todoID.setText(model.getId());

                        holder.checkToDoButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Завершение задачи
                                String deleteItem = holder.todoID.getText().toString();
                                Query deleteToDo = todoDatabase.child(DatabasePath.TODO_PATH).child(userID).orderByKey().equalTo(deleteItem);
                                deleteToDo.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot todoSnapshot : dataSnapshot.getChildren()) {
                                            todoSnapshot.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                Toast toast = Toast.makeText(getContext(), section + " click" + holder.todoID.getText(), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public todoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                        return new todoViewHolder(inflater.inflate(R.layout.item_todo, viewGroup, false));
                    }
                };
            }

            mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                    int lastVisiblePosition = todoLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                    // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                    // to the bottom of the list to show the newly added message.
                    if (lastVisiblePosition == -1 ||
                            (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                        todoList.scrollToPosition(positionStart);
                    }
                }
            });

            todoList.setLayoutManager(todoLinearLayoutManager);
            todoList.setAdapter(mFirebaseAdapter);


           // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
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

    public static class todoViewHolder extends RecyclerView.ViewHolder {
        TextView titleToDo;
        TextView descriptionToDo;
        TextView senderNameToDo;
        TextView fromToDO;
        TextView todoID;
        ImageButton checkToDoButton;


        todoViewHolder(View v) {
            super(v);
            titleToDo = itemView.findViewById(R.id.titleToDo);
            descriptionToDo = itemView.findViewById(R.id.descriptionToDo);
            senderNameToDo = itemView.findViewById(R.id.senderNameToDo);
            fromToDO = itemView.findViewById(R.id.fromToDo);
            checkToDoButton = itemView.findViewById(R.id.checkToDoButton);
            todoID = itemView.findViewById(R.id.todoID);
        }
    }

    private void openDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("UID", userID);

        ExampleCreateToDo exampleCreateToDo = new ExampleCreateToDo();
        exampleCreateToDo.setArguments(bundle);
        exampleCreateToDo.show(getSupportFragmentManager(), "example dialog");
    }

}
