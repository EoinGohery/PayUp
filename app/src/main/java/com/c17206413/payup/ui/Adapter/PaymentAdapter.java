package com.c17206413.payup.ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.c17206413.payup.R;
import com.c17206413.payup.ui.Model.User;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private final Context mContext;
    private final List<User> mUsers;
    private final UserListener userListener;

    public PaymentAdapter(Context mContext, List<User> mUsers, UserListener userListener) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view, userListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageUrl().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profile_image);
        }
        if (user.getSelected()) {
            holder.selected_image.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.selected_image.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView username;
        public ImageView profile_image;
        public ImageView selected_image;
        public UserListener userListener;

        public ViewHolder(View itemView, UserListener userListener) {
            super(itemView);

            this.userListener = userListener;

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            selected_image = itemView.findViewById(R.id.selected_star);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            userListener.onUserClick(getAdapterPosition());
        }
    }

    public interface UserListener {
        void onUserClick(int position);
    }
}
