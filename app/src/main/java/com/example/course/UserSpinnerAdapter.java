package com.example.course;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

public class UserSpinnerAdapter extends ArrayAdapter<UserSpinnerItem> {

    private String DEFAULT_TEXT = "Выберите пользователя";

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    public UserSpinnerAdapter(Context context,@Nullable ArrayList<UserSpinnerItem> userList) {
        super(context, 0, Objects.requireNonNull(userList));
    }

    private View initView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.users_spinner_row, parent, false
            );
        }

        ImageView  imageViewUser = convertView.findViewById(R.id.image_view_user);
        TextView textViewName = convertView.findViewById(R.id.text_view_name);

        UserSpinnerItem userSpinnerItem = getItem(position);

        if (userSpinnerItem != null) {

            if (userSpinnerItem.getUrl() != null) {
                Glide.with(getContext())
                        .load(userSpinnerItem.getUrl())
                        .into(imageViewUser);
                imageViewUser.setVisibility(View.VISIBLE);
            } else {
                Glide.with(getContext())
                        .load(userSpinnerItem.getImage())
                        .into(imageViewUser);
                imageViewUser.setVisibility(View.VISIBLE);
            }

//            if (userSpinnerItem.getUserName().equals(DEFAULT_TEXT)) imageViewUser.setVisibility(View.INVISIBLE);

            textViewName.setText(userSpinnerItem.getUserName());
        }

        return convertView;
    }
}
