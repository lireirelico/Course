package com.example.course;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

public class DialogueRecyclerAdapter extends RecyclerView.Adapter<DialogueActivity.MessageViewHolder> implements Filterable {

    private List<Message> mMessage;
    private List<Message> mMessageFiltered;
    private DatabaseReference messageDatabase;
    private int mLevel;
    private Context context;
    private String UID;
    private String mPhoto;
    private ViewGroup.LayoutParams params;

    public DialogueRecyclerAdapter(List<Message> messages, String UID, String mPhoto) {
        this.mMessage = messages;
        this.mMessageFiltered = messages;
        this.UID = UID;
        this.mPhoto = mPhoto;
    }

    @NonNull
    @Override
    public DialogueActivity.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView = inflater.inflate(R.layout.item_message, viewGroup, false);

        DialogueActivity.MessageViewHolder viewHolder = new DialogueActivity.MessageViewHolder(userView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DialogueActivity.MessageViewHolder messageViewHolder, final int i) {
        final Message message = mMessageFiltered.get(i);

        messageDatabase = FirebaseDatabase.getInstance().getReference();

        if (message.getText() != null) {
            //Сообщения пользователя
            if (message.getSide().equals(UID)) {
                messageViewHolder.myMessageLinearLayout.setVisibility(View.VISIBLE);
                messageViewHolder.messageLinearLayout.setVisibility(View.GONE);

                String mes = null;
                try {
                    mes = MyChiper.decrypt(message.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                messageViewHolder.myMessageTextView.setText(mes);

                if (mPhoto != null) {
                    Glide.with(context)
                            .load(mPhoto)
                            .into(messageViewHolder.myMessengerImageView);
                }
                else
                    messageViewHolder.myMessengerImageView.setImageDrawable(ContextCompat.getDrawable(context,
                            R.drawable.ic_account_circle_black_36dp));

                if (message.getTime() != null) {
                    messageViewHolder.myTimeTextView.setText(message.getTime());
                    messageViewHolder.myTimeTextView.setVisibility(View.VISIBLE);
                }
                else
                    messageViewHolder.myTimeTextView.setVisibility(View.GONE);

            }
            //Сообщения отправителя
            else
            {
                Query firebaseSearchQuery = messageDatabase.child(DatabasePath.USERS_PATH).orderByKey().equalTo(message.getSide());

                firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                            Users user = userSnapshot.getValue(Users.class);
                            String name = user.getUserName();
                            if (user != null) {
                                messageViewHolder.messageLinearLayout.setVisibility(View.VISIBLE);
                                messageViewHolder.myMessageLinearLayout.setVisibility(View.GONE);

                                String mes = null;
                                try {
                                    mes = MyChiper.decrypt(message.getText());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                messageViewHolder.messageTextView.setText(mes);

                                if (user.getPhoto() != null) {
                                    Glide.with(context)
                                            .load(user.getPhoto())
                                            .into(messageViewHolder.messengerImageView);
                                } else
                                    messageViewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(context,
                                            R.drawable.ic_account_circle_black_36dp));

                                if (message.getTime() != null) {
                                    messageViewHolder.timeTextView.setText(message.getTime());
                                    messageViewHolder.timeTextView.setVisibility(View.VISIBLE);
                                }
                                else
                                    messageViewHolder.timeTextView.setVisibility(View.GONE);
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

    @Override
    public int getItemCount() {
        return mMessageFiltered.size();
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
                    mMessageFiltered = mMessage;
                } else {
                    List<Message> filteredList = new ArrayList<>();
                    for (Message row : mMessage) {
                        //Берем нужные данные
                        try {
                            if (MyChiper.decrypt(row.getText()).toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    mMessageFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mMessageFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mMessageFiltered = (ArrayList<Message>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
