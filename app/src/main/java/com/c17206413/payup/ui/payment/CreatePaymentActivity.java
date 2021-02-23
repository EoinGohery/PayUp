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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.MainActivity;
import com.c17206413.payup.R;
import com.c17206413.payup.ui.Adapter.UserAdapter;
import com.c17206413.payup.ui.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.stripe.android.PaymentConfiguration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CreatePaymentActivity extends AppCompatActivity implements UserAdapter.UserListener {

    private FirebaseFirestore db;

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

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51HnPJaAXocUznruHqwf1wdNuZeIEEkX9ODwT0yuhtsv9nFPoghcpWbRLDcq3GU0k7g3RlPwCQGhCHVcMPe9nmoqB00JWK66tDF"
        );

        setContentView(R.layout.activity_create_payment);

        db = FirebaseFirestore.getInstance();

        searchRecycler = (RecyclerView) findViewById(R.id.user_recycler);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));

        priceInput = (TextInputLayout) findViewById(R.id.priceLayout);
        nameInput = (TextInputLayout) findViewById(R.id.serviceNameLayout);

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

                    pricePP.setText(String.format("%s%s", getResources().getString(R.string.pricePerP), NumberFormat.getCurrencyInstance().format((perPerson/ 100 / (addedUsers.size() + 1)))));

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
            String serviceName = Objects.requireNonNull(nameInput.getEditText()).getText().toString();
            createGroupPayment(serviceName, "Eur");
            //TODO (Create get currency)
        });

        mUsers = new ArrayList<>();
        addedUsers = new ArrayList<>();
        readUsers();
    }

    private void createGroupPayment(String serviceName, String currency) {
        if (!validateNameForm(serviceName)) {
            return;
        }

        for (int i=0; i < addedUsers.size(); i++) {
            User user = addedUsers.get(i);
            String uid = user.getId();
            String amount = String.valueOf(Math.round((perPerson / (addedUsers.size() + 1))));

            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("user_id", uid);
            paymentDetails.put("currency", currency);
            paymentDetails.put("amount", amount);
            paymentDetails.put("service_name", serviceName);
            paymentDetails.put("active", true);

            db.collection("users").document(MainActivity.getUid()).collection("incoming")
                    .document()
                    .set(paymentDetails)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }
        finish();
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
    private boolean validateNameForm(String serviceName) {
        boolean valid = true;

        if (serviceName.isEmpty()) {
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
        pricePP.setText(String.format("%s%s", getResources().getString(R.string.pricePerP), NumberFormat.getCurrencyInstance().format((perPerson/ 100 / (addedUsers.size() + 1)))));
    }
}
