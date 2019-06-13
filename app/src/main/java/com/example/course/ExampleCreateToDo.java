package com.example.course;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ExampleCreateToDo extends AppCompatDialogFragment {

    private DatabaseReference firebaseDatabase;
    private String UID;
    private EditText description;
    private EditText title;
    private boolean meFlag;
    private String mID;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getArguments();
        if (bundle != null) {
            UID = bundle.getString("UID");
        }

        mID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (UID.equals(mID)) meFlag = true;
        else meFlag = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_create_todo, null);

        description = view.findViewById(R.id.descriptionEditText);
        title = view.findViewById(R.id.titleEditText);

        builder.setView(view)
                .setTitle("Новая задача")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!description.getText().toString().equals("") && !title.getText().toString().equals("")) {

                            String key = firebaseDatabase.child(DatabasePath.TODO_PATH).child(UID).push().getKey();

                            final ToDo toDo = new ToDo(key, description.getText().toString(), title.getText().toString(), UID, meFlag);
                            firebaseDatabase.child(DatabasePath.TODO_PATH).child(UID).child(key).setValue(toDo);

                            Toast.makeText(getContext(), "Отправлено",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Пустые поля ввода новой задачи",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return builder.create();
    }
}
