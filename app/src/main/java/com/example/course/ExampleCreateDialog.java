package com.example.course;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class ExampleCreateDialog extends AppCompatDialogFragment {
    private EditText editMessage;
    private Spinner usersSpinner;
    private String  selectUserId;
    private DatabaseReference firebaseDatabase;
    private ArrayList<UserSpinnerItem> mUserList = new ArrayList<>();
    private UserSpinnerAdapter mAdapter;
    private String DEFAULT_TEXT = "Выберите пользователя";
    private String DEFAULT_USERID = "NULL";
    private int DEFAULT_IMAGE = R.drawable.empty24dp;
    private String UID;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        Bundle bundle = getArguments();
        if (bundle != null) {
            UID = bundle.getString("UID");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_create_dialog, null);

        editMessage = view.findViewById(R.id.spinner_edit_message);
        usersSpinner = view.findViewById(R.id.spinner_users);

        mUserList = new ArrayList<>();
        initList(DEFAULT_TEXT, DEFAULT_USERID, DEFAULT_IMAGE);

        builder.setView(view)
                .setTitle("Dialog")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!selectUserId.equals(DEFAULT_USERID) && !editMessage.getText().toString().equals("")) {

                            String mes = null;
                            try {
                                mes = MyChiper.encrypt(editMessage.getText().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Query checkDialog = firebaseDatabase.child(DatabasePath.CHATS_PATH)
                                    .child(UID).orderByChild("destination").equalTo(selectUserId);

                            final String finalMes = mes;
                            checkDialog.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    int count = 0;
                                    for(DataSnapshot updateChat : dataSnapshot.getChildren()){
                                        final Chats chat = updateChat.getValue(Chats.class);

                                        assert chat != null;
                                        chat.setLastMessage(finalMes);

                                        final String deleteKey = updateChat.getRef().getKey();

                                        firebaseDatabase.child(DatabasePath.CHATS_PATH).child(UID).child(deleteKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseDatabase.child(DatabasePath.CHATS_PATH).child(selectUserId).child(deleteKey).removeValue();
                                            }
                                        });

                                        final Chats tmp = new Chats(UID, finalMes, chat.getChatId());
                                        final String newKey = firebaseDatabase.child(DatabasePath.CHATS_PATH).child(UID).push().getKey();
                                        assert newKey != null;
                                        firebaseDatabase.child(DatabasePath.CHATS_PATH).child(UID).child(newKey).setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseDatabase.child(DatabasePath.CHATS_PATH).child(selectUserId).child(newKey).setValue(tmp);

                                                Calendar calendar = Calendar.getInstance();
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                                String time = dateFormat.format(calendar.getTime());

                                                Message message = new Message(finalMes, UID, time);
                                                firebaseDatabase.child(DatabasePath.MESSAGE_PATH)
                                                        .child(chat.getChatId()).push().setValue(message);
                                            }
                                        });

                                        count++;
                                    }

                                    //Создание чата если его нет
                                    if (count == 0){
                                        //Создание нового чата
                                        final String key = dataSnapshot.getRef().push().getKey();
                                        final Chats chat = new Chats(selectUserId, finalMes, key);

                                        if (key != null) {
                                            firebaseDatabase.child(DatabasePath.CHATS_PATH).child(UID).child(key).setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Chats destinationChat = new Chats(UID, finalMes, chat.getChatId());
                                                    firebaseDatabase.child(DatabasePath.CHATS_PATH).child(selectUserId).child(key).setValue(destinationChat);

                                                    //Создание сообщения
                                                    Calendar calendar = Calendar.getInstance();
                                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                                    String time = dateFormat.format(calendar.getTime());

                                                    Message message = new Message(finalMes, UID, time);
                                                    firebaseDatabase.child(DatabasePath.MESSAGE_PATH)
                                                            .child(chat.getChatId()).push().setValue(message);
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            Toast.makeText(getContext(), "Пустые поля ввода",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Query firebaseSearchQuery = firebaseDatabase.child(DatabasePath.USERS_PATH);

        firebaseSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserList.clear();
                initList(DEFAULT_TEXT, DEFAULT_USERID, DEFAULT_IMAGE);

                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    Users user = userSnapshot.getValue(Users.class);

                    assert user != null;
                    if (user.getUserId() != null){
                        if (user.getPhoto() != null) {
                            initList(user.getUserName(), user.getUserId(), user.getPhoto());
                        } else {
                            initList(user.getUserName(), user.getUserId(), R.drawable.ic_account_circle_black_36dp);
                        }
                    }

                    mAdapter = new UserSpinnerAdapter(getContext(), mUserList);
                    usersSpinner.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        usersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserSpinnerItem selectItem = (UserSpinnerItem) parent.getItemAtPosition(position);
                selectUserId = selectItem.getUserID();
                Toast toast = Toast.makeText(getContext(), selectUserId, Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return builder.create();
    }

    private void initList(String name, String userID, int image) {
        mUserList.add(new UserSpinnerItem(name, userID, image));
    }

    private void initList(String name, String userID, String image) {
        mUserList.add(new UserSpinnerItem(name, userID, image));
    }

    private void initList(String name, String userID) {
        mUserList.add(new UserSpinnerItem(name, userID));
    }
}
