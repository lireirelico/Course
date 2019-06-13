package com.example.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private String userID;
    private DatabaseReference usersDatabase;
    private TextView employeeTextView;
    private TextView mailTextView;
    private TextView usernameTextView;
    private TextView phoneTextView;
    private TextView employeeTextView2;
    private TextView mailTextView2;
    private TextView usernameTextView2;
    private TextView phoneTextView2;
    private ImageView photoProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Objects.requireNonNull(getSupportActionBar()).hide();

        //Элементы активностей
        phoneTextView = findViewById(R.id.phoneNumberProfileTextView);
        mailTextView = findViewById(R.id.mailProfileTextView);
        usernameTextView = findViewById(R.id.nameProfileTextView);
        employeeTextView = findViewById(R.id.employeeProfileTextView);
        phoneTextView2 = findViewById(R.id.phoneNumberProfileTextView2);
        mailTextView2 = findViewById(R.id.mailProfileTextView2);
        employeeTextView2 = findViewById(R.id.employeeProfileTextView2);
        photoProfileImageView = findViewById(R.id.userProfileImageView);

        //Подключаем базу данных
        usersDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        Toast toast = Toast.makeText(getApplicationContext(), userID, Toast.LENGTH_SHORT);
        toast.show();

        Query query = usersDatabase.child(DatabasePath.USERS_PATH).orderByKey().equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    Users user = userSnapshot.getValue(Users.class);

                    assert user != null;
                    if (user.getUserName() != null) {
                        usernameTextView.setText(user.getUserName());
                    }

                    if (user.getPhoneNumber() != null){
                        phoneTextView.setText(user.getPhoneNumber());
                        phoneTextView.setVisibility(TextView.VISIBLE);
                        phoneTextView2.setVisibility(TextView.VISIBLE);
                    } else {
                        phoneTextView.setVisibility(TextView.GONE);
                        phoneTextView2.setVisibility(TextView.GONE);

                    }

                    if (user.getEmployee() != null){
                        employeeTextView.setText(user.getEmployee());
                        employeeTextView.setVisibility(TextView.VISIBLE);
                        employeeTextView2.setVisibility(TextView.VISIBLE);

                    } else {
                        employeeTextView.setVisibility(TextView.GONE);
                        employeeTextView2.setVisibility(TextView.GONE);
                    }

                    if (user.getMail() != null) {
                        mailTextView.setText(user.getMail());
                        mailTextView.setVisibility(TextView.VISIBLE);
                        mailTextView2.setVisibility(TextView.VISIBLE);
                    } else {
                        mailTextView.setVisibility(TextView.GONE);
                        mailTextView2.setVisibility(TextView.GONE);
                    }

                    if (user.getPhoto() != null) {
                        Glide.with(UserProfileActivity.this)
                                .load(user.getPhoto())
                                .into(photoProfileImageView);
                    } else {

                        photoProfileImageView.setImageDrawable(ContextCompat
                                .getDrawable(UserProfileActivity.this,
                                        R.drawable.ic_account_circle_black_36dp));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void clickBackProfileButton(View view) {
        finish();
    }
}
