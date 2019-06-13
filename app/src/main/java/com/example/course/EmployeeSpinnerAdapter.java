package com.example.course;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class EmployeeSpinnerAdapter extends ArrayAdapter<Position> {
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

    public EmployeeSpinnerAdapter(Context context,@Nullable ArrayList<Position> employeeList) {
        super(context, 0, Objects.requireNonNull(employeeList));
    }

    private View initView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.employee_spinner_row, parent, false
            );
        }

        TextView  employeeId = convertView.findViewById(R.id.employeeId);
        TextView employeeName = convertView.findViewById(R.id.employeeName);
        TextView employeeLevel = convertView.findViewById(R.id.employeeLevel);

        Position userSpinnerItem = getItem(position);

        if (userSpinnerItem != null) {
            employeeId.setText(userSpinnerItem.getId());
            employeeName.setText(userSpinnerItem.getName());
            employeeLevel.setText(String.valueOf(userSpinnerItem.getLevel()));
        }

        return convertView;
    }
}

