package com.example.course;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    private String UID;
    private DatabaseReference ref;
    private EditText usernameEditText;
    private EditText mailEditText;
    private EditText phoneEditText;
    private ImageButton saveButton;
    private ImageView userPhotoImageView;
    private StorageReference mStorageRef;
    private ProgressBar uploadPhotoProgressBar;
    private String phone;
    private String mail;
    private Users user; 
    private String DEFAULT = "отсутствует";
    private Uri mImageUri;
    private String userName;
    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Objects.requireNonNull(getSupportActionBar()).hide();

        usernameEditText = findViewById(R.id.usernameSettingEditText);
        mailEditText = findViewById(R.id.mailSetiingEditText);
        phoneEditText = findViewById(R.id.phoneSetiingEditText);
        userPhotoImageView = findViewById(R.id.userSettingImageView);
        uploadPhotoProgressBar = findViewById(R.id.uploadPhotoProgressBar);
        saveButton = findViewById(R.id.saveButton);

        uploadPhotoProgressBar.setVisibility(View.INVISIBLE);
        phoneEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (phoneEditText.getText().toString().equals(DEFAULT)) {
                    phoneEditText.setText("+375");
                }
                return false;
            }
        });
        mailEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mailEditText.getText().toString().equals(DEFAULT)) {
                    mailEditText.setText("");
                }
                return false;
            }
        });

        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");

        ref = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference(UID);

        final Query query = ref.child(DatabasePath.USERS_PATH).orderByKey().equalTo(UID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    user = userSnapshot.getValue(Users.class);
                    assert user != null;
                    usernameEditText.setText(user.getUserName());
                    userName = user.getUserName();

                    if (user.getPhoneNumber() == null) phoneEditText.setText(DEFAULT);
                    else {
                        phone = user.getPhoneNumber();
                        phoneEditText.setText(phone);
                    }

                    if (user.getMail() == null) mailEditText.setText(DEFAULT);
                    else {
                        mail = user.getMail();
                        mailEditText.setText(mail);
                    }

                    if (user.getPhoto() != null) {
                        Glide.with(SettingActivity.this)
                                .load(user.getPhoto())
                                .into(userPhotoImageView);
                    } else {
                        Glide.with(SettingActivity.this)
                                .load(R.drawable.ic_account_circle_black_36dp)
                                .into(userPhotoImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка загрузки базы данных",
                        Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void uploadPhoto(View view) {
        uploadFile();
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

            Glide.with(this).load(mImageUri).into(userPhotoImageView);
           // Picasso.with(this).load(mImageUri).into(userPhotoImageView);
        }
    }

    public void choosePhoteButton(View view) {
        openFileChooser();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child("mPhoto" + "."
                    + getFileExtension(mImageUri));

            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    uploadPhotoProgressBar.setProgress(0);
                                }
                            }, 5000);

                            Toast.makeText(getApplicationContext(), "Загрузка заверщена", Toast.LENGTH_SHORT).show();
                            uploadPhotoProgressBar.setVisibility(View.INVISIBLE);

                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    user.setPhoto(uri.toString());
                                    ref.child(DatabasePath.USERS_PATH).child(UID).setValue(user);
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
                            uploadPhotoProgressBar.setVisibility(View.VISIBLE);
                            uploadPhotoProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "Файл не выбран", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBackSettingButton(View view) {
        finish();
    }

    public void saveButton(View view) {
        boolean flag = false;

        if (!usernameEditText.getText().toString().equals(userName)
                || !phoneEditText.getText().toString().equals(user.getPhoneNumber())
                || !mailEditText.getText().toString().equals(user.getMail())) {

            if (isValidEmail(mailEditText.getText().toString())
                    && !mailEditText.getText().toString().equals(user.getMail())){
                user.setMail(mailEditText.getText().toString());
                flag = true;
            }
            else if (!isValidEmail(mailEditText.getText().toString()))
            {
                Toast.makeText(this, "Электронная почта введена не правильно",
                        Toast.LENGTH_SHORT).show();
            }

            if (isValidCellPhone(phoneEditText.getText().toString())
                    && !phoneEditText.getText().toString().equals(user.getPhoneNumber())) {
                user.setPhoneNumber(phoneEditText.getText().toString());
                flag = true;
            }
            else if (isValidCellPhone(phoneEditText.getText().toString())) {
                Toast.makeText(this, "Телефон введен не правильно",
                        Toast.LENGTH_SHORT).show();
            }

            if (!usernameEditText.getText().toString().equals("")
                    && !usernameEditText.getText().toString().equals(userName)) {
                user.setUserName(usernameEditText.getText().toString());
                flag = true;
            }

            if (flag) {
                ref.child(DatabasePath.USERS_PATH).child(UID).setValue(user);
                finish();
                Toast.makeText(this, "Сохранено",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Данные не изменены",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public boolean isValidCellPhone(String number)
    {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }
}
