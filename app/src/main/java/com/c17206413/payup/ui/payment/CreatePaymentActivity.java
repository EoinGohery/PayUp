package com.c17206413.payup.ui.payment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.R;
import com.c17206413.payup.ui.Adapter.UserAdapter;
import com.c17206413.payup.ui.Model.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreatePaymentActivity extends AppCompatActivity implements UserAdapter.UserListener {
    private RecyclerView searchRecycler;
    private RecyclerView addedRecycler;

    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<User> addedUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_payment);

        searchRecycler = (RecyclerView) findViewById(R.id.user_recycler);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));

        mUsers = new ArrayList<>();
        readUsers();
    }

    private void readUsers() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mUsers.clear();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String id = document.getId();
                            String username = document.getString("name");
                            //String profileUrl = document.getString("ProfileUrl");
                            User user = new User(id, username, "default");
                            assert firebaseUser != null;
                            if (!user.getId().equals(firebaseUser.getUid())) {
                                mUsers.add(user);
                            }
                        }
                        userAdapter = new UserAdapter(this, mUsers, this);
                        searchRecycler.setAdapter(userAdapter);

                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Receive Document Failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });


    }

    @Override
    public void onUserClick(int position) {
        mUsers.get(position).swapSelected();
        userAdapter = new UserAdapter(this, mUsers, this);
        searchRecycler.setAdapter(userAdapter);
    }
}
