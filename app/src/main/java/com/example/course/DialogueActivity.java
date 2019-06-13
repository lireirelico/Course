package com.example.course;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DialogueActivity extends AppCompatActivity {

    //todo сделать определение стороны передачи данных

    private String CHATS_PATH = "Chats";
    private String MESSAGE_PATH = "Message";
    private String USERS_PATH = "Users";
    private String UserID;
    private String DIALOG_ID;
    private DatabaseReference firebaseDatabase;
    private DatabaseReference messageDatabase;
    private ImageButton mSendButton;
    private EditText mMessageEditText;
    private RecyclerView messageList;
    private RecyclerView messageListFull;
    private LinearLayoutManager messageLinearLayoutManager;
    private MessageViewHolder forSearch;
    private RecyclerView chatList;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private String mPhoto;
    private String dialogName;
    private TextView usernameDialogTextView;
    private boolean focusFlag = true;
    private ArrayList<MessageViewHolder> mList;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private ImageView addMessageImageView;
    private StorageReference mStorageRef;
    private DialogueRecyclerAdapter adapter;
    private ArrayList<Message> messageArrayList = new ArrayList<>();

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);

        Objects.requireNonNull(getSupportActionBar()).hide();

        usernameDialogTextView = findViewById(R.id.usernameDialogTextView);
        messageListFull = findViewById(R.id.messageSearchRecyclerView);

        final SearchView searchView = (SearchView) findViewById(R.id.dialogSearch);
        addMessageImageView = findViewById(R.id.addMessageImageView);
        addMessageImageView.setVisibility(View.GONE);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (focusFlag) {
                    usernameDialogTextView.setVisibility(View.GONE);
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
                usernameDialogTextView.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = messageList.getLayoutParams();

                params.height = 0;
                params.width = 0;
                messageListFull.setVisibility(View.GONE);
                messageListFull.setLayoutParams(params);

                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                messageList.setVisibility(View.VISIBLE);
                messageList.setLayoutParams(params);
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
                ViewGroup.LayoutParams params = messageList.getLayoutParams();
                params.height = 0;
                params.width = 0;
                messageList.setVisibility(View.GONE);
                messageList.setLayoutParams(params);

                searchMessage(newText);

                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                messageListFull.setVisibility(View.VISIBLE);
                messageListFull.setLayoutParams(params);
                return false;
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        DIALOG_ID = intent.getStringExtra("dialogID");
        UserID = intent.getStringExtra("Uid");
        mPhoto = intent.getStringExtra("mPhoto");
        dialogName = intent.getStringExtra("dialogName");

        //Рукуслер
        messageLinearLayoutManager = new LinearLayoutManager(this);
        messageLinearLayoutManager.setStackFromEnd(true);

        usernameDialogTextView.setText(dialogName);

        //Подключаем базу данных
        messageDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Dialog").child(DIALOG_ID);

        messageArrayList.clear();

        Query query = messageDatabase.child(MESSAGE_PATH).child(DIALOG_ID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot message : dataSnapshot.getChildren()) {
                    Message tempMessage = message.getValue(Message.class);
                        messageArrayList.add(tempMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter = new DialogueRecyclerAdapter(messageArrayList, UserID, mPhoto);

        messageListFull.setAdapter(adapter);
        messageListFull.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        SnapshotParser<Message> parser = new SnapshotParser<Message>() {
            @NonNull
            @Override
            public Message parseSnapshot(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    message.setId(dataSnapshot.getKey());
                }
                return message;
            }
        };

        //Откуда достаем наши чаты
        final DatabaseReference chatRef = messageDatabase.child(CHATS_PATH).child(DIALOG_ID);
        final DatabaseReference userRef = messageDatabase.child(USERS_PATH);
        final DatabaseReference messageRef = messageDatabase.child(MESSAGE_PATH).child(DIALOG_ID);

        //Инициализация списка
        messageList = findViewById(R.id.messageRecyclerView);
        messageList.setHasFixedSize(false);
        messageList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messageRef, parser)
                        .build();

        //Адаптер от гугла
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {


            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            //Получаем данные по чатам
            @Override
            protected void onBindViewHolder(@NonNull final MessageViewHolder viewHolder,
                                            final int position,
                                            @NonNull final Message message) {

                mList = new ArrayList<>();

                //Обработка нажатия на элемент
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message clickMessage = getItem(position);
                        Toast toast = Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_LONG);
                        toast.show();
                    }
                });

                if (message.getText() != null) {
                    //Сообщения пользователя
                    if (message.getSide().equals(UserID)) {
                        viewHolder.myMessageLinearLayout.setVisibility(View.VISIBLE);
                        viewHolder.messageLinearLayout.setVisibility(View.GONE);

                        String mes = null;
                        try {
                            mes = MyChiper.decrypt(message.getText());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        viewHolder.myMessageTextView.setText(mes);

                        if (mPhoto != null) {
                            Glide.with(DialogueActivity.this)
                                    .load(mPhoto)
                                    .into( viewHolder.myMessengerImageView);
                        }
                        else
                        viewHolder.myMessengerImageView.setImageDrawable(ContextCompat.getDrawable(DialogueActivity.this,
                                R.drawable.ic_account_circle_black_36dp));

                        if (message.getTime() != null) {
                            viewHolder.myTimeTextView.setText(message.getTime());
                            viewHolder.myTimeTextView.setVisibility(View.VISIBLE);
                        }
                        else
                            viewHolder.myTimeTextView.setVisibility(View.GONE);

                        if (message.getImageUrl() != null) {
                            Glide.with(DialogueActivity.this)
                                    .load(message.getImageUrl())
                                    .into( viewHolder.myMessageImageView);
                        }

                        mList.add(viewHolder);
                    }
                    //Сообщения отправителя
                    else
                    {
                        Query firebaseSearchQuery = messageDatabase.child(USERS_PATH).orderByKey().equalTo(message.getSide());

                        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                    Users user = userSnapshot.getValue(Users.class);
                                    String name = user.getUserName();
                                    if (user != null) {
                                        viewHolder.messageLinearLayout.setVisibility(View.VISIBLE);
                                        viewHolder.myMessageLinearLayout.setVisibility(View.GONE);

                                        String mes = null;
                                        try {
                                            mes = MyChiper.decrypt(message.getText());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        viewHolder.messageTextView.setText(mes);

                                        if (user.getPhoto() != null) {
                                            Glide.with(DialogueActivity.this)
                                                    .load(user.getPhoto())
                                                    .into(viewHolder.messengerImageView);
                                        } else
                                            viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(DialogueActivity.this,
                                                    R.drawable.ic_account_circle_black_36dp));

                                        if (message.getTime() != null) {
                                            viewHolder.timeTextView.setText(message.getTime());
                                            viewHolder.timeTextView.setVisibility(View.VISIBLE);
                                        }
                                        else
                                            viewHolder.timeTextView.setVisibility(View.GONE);

                                        if (message.getImageUrl() != null) {
                                            Glide.with(DialogueActivity.this)
                                                    .load(message.getImageUrl())
                                                    .into( viewHolder.messageImageView);
                                        }

                                        mList.add(viewHolder);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = messageLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    messageList.scrollToPosition(positionStart);
                }
            }
        });

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        /*mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});*/
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = findViewById(R.id.sendButton);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (!mMessageEditText.getText().toString().equals("") || mImageUri != null) {

                    String mes = null;

                    try {
                        mes = MyChiper.encrypt(mMessageEditText.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    final String time = dateFormat.format(calendar.getTime());

                   if (mImageUri != null) {
                       addMessageImageView.setVisibility(View.GONE);

                       String key = messageDatabase.child(MESSAGE_PATH).child(DIALOG_ID).push().getKey();

                       final StorageReference fileReference = mStorageRef.child(key + "."
                               + getFileExtension(mImageUri));

                       final String finalMes1 = mes;
                       fileReference.putFile(mImageUri)
                               .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                   @Override
                                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                       Toast.makeText(getApplicationContext(), "Загрузка заверщена", Toast.LENGTH_SHORT).show();

                                       taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                           @Override
                                           public void onSuccess(Uri uri) {
                                               Message message = new Message(finalMes1, UserID, time, uri.toString());
                                               messageDatabase.child(MESSAGE_PATH).child(DIALOG_ID).push().setValue(message);
                                           }
                                       });
                                   }
                               })
                               .addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       Toast.makeText(getApplicationContext(), "Ошибка загрузки файла",
                                               Toast.LENGTH_SHORT).show();
                                   }
                               })
                               .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                   @Override
                                   public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                       double progress = (100.0 * taskSnapshot.getBytesTransferred()
                                               / taskSnapshot.getTotalByteCount());
                                   }
                               });
                   } else {
                       Message message = new Message(mes, UserID, time);
                       messageDatabase.child(MESSAGE_PATH).child(DIALOG_ID).push().setValue(message);
                   }

                    Query updateChat = messageDatabase.child(CHATS_PATH).child(UserID).orderByChild("chatId").equalTo(DIALOG_ID);
                    final String finalMes = mes;
                    updateChat.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot updateChat : dataSnapshot.getChildren()) {
                                final Chats chat = updateChat.getValue(Chats.class);
                                assert chat != null;
                                if (mImageUri != null) chat.setLastMessage("Изображение");
                                else chat.setLastMessage(finalMes);

                                final String deleteChat = updateChat.getRef().getKey();
                                updateChat.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseDatabase.child(DatabasePath.CHATS_PATH).child(chat.getDestination()).child(deleteChat).removeValue();
                                    }
                                });

                                final Chats destinationChat = new Chats(UserID, chat.getLastMessage(), chat.getChatId());

                                final String newKey = firebaseDatabase.child(DatabasePath.CHATS_PATH).child(UserID).push().getKey();
                                assert newKey != null;
                                firebaseDatabase.child(DatabasePath.CHATS_PATH).child(UserID).child(newKey).setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseDatabase.child(DatabasePath.CHATS_PATH).child(chat.getDestination()).child(newKey).setValue(destinationChat);
                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    mMessageEditText.setText("");
                    mImageUri = null;
                }
            }
        });


        messageList.setLayoutManager(messageLinearLayoutManager);
        messageList.setAdapter(mFirebaseAdapter);
    }

    public void clickBackDialogButton(View view) {
        finish();
    }

    public void clickImageSendButton(View view) {
        openFileChooser();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView messageTextView;
        ImageView messageImageView;
        CircleImageView messengerImageView;
        RelativeLayout messageLinearLayout;
        TextView timeTextView;

        RelativeLayout myMessageLinearLayout;
        TextView myMessageTextView;
        ImageView myMessageImageView;
        CircleImageView myMessengerImageView;
        TextView myTimeTextView;

        MessageViewHolder(View v) {
            super(v);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messengerImageView = itemView.findViewById(R.id.messengerImageView);
            messageLinearLayout = itemView.findViewById(R.id.messageLinearLayout);
            timeTextView = itemView.findViewById(R.id.timeTextView);

            myMessageLinearLayout = itemView.findViewById(R.id.myMessageLinearLayout);
            myMessageTextView = itemView.findViewById(R.id.myMessageTextView);
            myMessageImageView = itemView.findViewById(R.id.myMessageImageView);
            myMessengerImageView = itemView.findViewById(R.id.myMessengerImageView);
            myTimeTextView = itemView.findViewById(R.id.myTimeTextView);
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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            mImageUri = data.getData();

            Glide.with(this).load(mImageUri).into(addMessageImageView);
            addMessageImageView.setVisibility(View.VISIBLE);
            // Picasso.with(this).load(mImageUri).into(userPhotoImageView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(mImageUri + "."
                    + getFileExtension(mImageUri));

            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Шкала прогресса uploadPhotoProgressBar.setProgress(0);
                                }
                            }, 5000);

                            Toast.makeText(getApplicationContext(), "Загрузка заверщена", Toast.LENGTH_SHORT).show();
                            //шкала прогресса uploadPhotoProgressBar.setVisibility(View.INVISIBLE);

                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //передать фото в сообщении user.setPhoto(uri.toString()
                                    //передать фото в облако ref.child(DatabasePath.USERS_PATH).child(UID).setValue(user);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            //прогресс бар uploadPhotoProgressBar.setVisibility(View.VISIBLE);
                            //прогресс бар uploadPhotoProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "Файл не выбран", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchMessage(String searchMessage) {
        adapter.getFilter().filter(searchMessage);
    }
}
