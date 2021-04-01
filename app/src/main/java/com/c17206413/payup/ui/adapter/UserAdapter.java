package com.c17206413.payup.ui.adapter;

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
import com.c17206413.payup.ui.model.User;

import java.util.List;

//recycler adapter for displaying user informatiion
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final Context mContext;
    //list of user objects
    private final List<User> mUsers;
    //click listener for recycler adapter
    private final UserListener userListener;

    //adapter constructor
    public UserAdapter(Context mContext, List<User> mUsers, UserListener userListener) {
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
        //set specific user information to each adapter object on view holder
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        //set user profile picture
        if (user.getImageUrl() == null) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profile_image);
        }
        //set whether the user has been selected or not using highlighted star image
        if (user.getSelected()) {
            holder.selected_image.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.selected_image.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    //gert the total number of items in list
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

            //user listener
            this.userListener = userListener;

            //UI elements
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            selected_image = itemView.findViewById(R.id.selected_star);

            //On click listener for entire object
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            userListener.onUserClick(getAdapterPosition());
        }
    }

    //payment listener interface (methods to be overwritten in create payemnt class)
    public interface UserListener {
        void onUserClick(int position);
    }
}
