package com.example.course;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UsersActivity.UsersViewHolder> implements Filterable {

    private List<Users> mUsers;
    private List<Users> mUsersFiltered;
    private DatabaseReference usersDatabase;
    private int mLevel;
    private Context context;
    private String UID;
    private ViewGroup.LayoutParams params;

    public UserRecyclerAdapter(List<Users> users, int mLevel, String UID) {
        this.mUsers = users;
        this.mUsersFiltered = users;
        this.mLevel = mLevel;
        this.UID = UID;
    }

    @NonNull
    @Override
    public UsersActivity.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView = inflater.inflate(R.layout.item_user, viewGroup, false);

        UsersActivity.UsersViewHolder viewHolder = new UsersActivity.UsersViewHolder(userView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersActivity.UsersViewHolder usersViewHolder, final int i) {
        final Users user = mUsersFiltered.get(i);

        usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Users clickUser = mUsersFiltered.get(i);

                //Передача данных в активности профиля
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userID", clickUser.getUserId());
                context.startActivity(intent);
            }
        });

        usersViewHolder.createUserToDoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Users clickUser = mUsersFiltered.get(i);
                Bundle bundle = new Bundle();
                bundle.putString("UID", clickUser.getUserId());

                ExampleCreateToDo exampleCreateToDo = new ExampleCreateToDo();
                exampleCreateToDo.setArguments(bundle);
                exampleCreateToDo.show(((AppCompatActivity)context).getSupportFragmentManager(), "example dialog");
            }
        });
        usersDatabase = FirebaseDatabase.getInstance().getReference();

        Query employee = usersDatabase.child(DatabasePath.POSITION_PATH).orderByKey().equalTo(user.getEmployee());

        employee.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot employeeSnapshot : dataSnapshot.getChildren()) {
                    Position employeePosition = employeeSnapshot.getValue(Position.class);
                    if (employeePosition != null) {
                        usersViewHolder.userEmployeeTextView.setText(employeePosition.getName());

                        usersViewHolder.userTextView.setText(user.getUserName());

                        if (user.getPhoto() != null) {
                            Glide.with(context)
                                    .load(user.getPhoto())
                                    .into(usersViewHolder.userImageView);
                        }
                        else Glide.with(context)
                                .load(R.drawable.ic_account_circle_black_36dp)
                                .into(usersViewHolder.userImageView);

                        int level = employeePosition.getLevel();

                        if (level < mLevel) {
                            usersViewHolder.createUserToDoButton.setVisibility(View.VISIBLE);
                        } else {
                            usersViewHolder.createUserToDoButton.setVisibility(View.GONE);
                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    @Override
    public int getItemCount() {
        return mUsersFiltered.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    mUsersFiltered = mUsers;
                } else {
                    List<Users> filteredList = new ArrayList<>();
                    for (Users row : mUsers) {
                        //Берем нужные данные
                        if (row.getUserName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    mUsersFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mUsersFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mUsersFiltered = (ArrayList<Users>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
