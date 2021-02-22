package com.c17206413.payup.ui.payment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.R;
import com.c17206413.payup.ui.Adapter.UserAdapter;
import com.c17206413.payup.ui.Model.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreatePaymentActivity extends AppCompatActivity implements UserAdapter.UserListener {
    private static final String TAG = "Payment Creation";
    private RecyclerView searchRecycler;

    private ProgressBar progressBar;

    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<User> addedUsers;

    private TextInputLayout nameInput;
    private TextInputLayout priceInput;

    private TextView pricePP;

    private String current = "";
    private double perPerson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_payment);

        searchRecycler = (RecyclerView) findViewById(R.id.user_recycler);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));

        priceInput = (TextInputLayout) findViewById(R.id.priceLayout);
        nameInput = (TextInputLayout) findViewById(R.id.nameLayout);

        pricePP = (TextView) findViewById(R.id.price_per_person);

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        Objects.requireNonNull(priceInput.getEditText()).setRawInputType(Configuration.KEYBOARD_12KEY);
        Objects.requireNonNull(priceInput.getEditText()).addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    priceInput.getEditText().removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,£€.]", "");

                    double parsed = Double.parseDouble(cleanString);
                    perPerson = parsed;

                    pricePP.setText("Price per person: " + NumberFormat.getCurrencyInstance().format((parsed / 100 / (addedUsers.size()+1))));

                    String formatted = NumberFormat.getCurrencyInstance().format((parsed/100));

                    current = formatted;
                    priceInput.getEditText().setText(formatted);
                    priceInput.getEditText().setSelection(formatted.length());

                    priceInput.getEditText().addTextChangedListener(this);
                }
            }

        });

        Button createPaymentButton= (Button) findViewById(R.id.create_payment_button);
        createPaymentButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            createGroupPayment();
        });

        mUsers = new ArrayList<>();
        addedUsers = new ArrayList<>();
        readUsers();
    }

    private void createGroupPayment() {
        if (!validateNameForm()) {
            return;
        }
        //TODO (create multiple paymentIntents for each user)
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
                            String profileUrl = document.getString("ProfileUrl");
                            if (profileUrl == null) {
                                profileUrl="default";
                            }
                            User user = new User(id, username, profileUrl);
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

    //validate service name correctness
    private boolean validateNameForm() {
        boolean valid = true;

        String emailString = Objects.requireNonNull(nameInput.getEditText()).getText().toString();
        if (emailString.isEmpty()) {
            nameInput.setError("Required.");
            valid = false;
        } else {
            nameInput.setError(null);
        }
        return valid;
    }

    @Override
    public void onUserClick(int position) {
        mUsers.get(position).swapSelected();
        userAdapter = new UserAdapter(this, mUsers, this);
        searchRecycler.setAdapter(userAdapter);
        countSelected();
    }

    private void countSelected() {
        addedUsers.clear();
        for (int i=0; i < mUsers.size(); i++) {
            User user = mUsers.get(i);
            if (user.getSelected()) {
                addedUsers.add(user);
            } else {
                addedUsers.remove(user);
            }
        }
        pricePP.setText("Price per person: " + NumberFormat.getCurrencyInstance().format((perPerson / 100 / (addedUsers.size()+1))));
    }
}
