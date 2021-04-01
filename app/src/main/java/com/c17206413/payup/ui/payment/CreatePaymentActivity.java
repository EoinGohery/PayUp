package com.c17206413.payup.ui.payment;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.MainActivity;
import com.c17206413.payup.R;
import com.c17206413.payup.ui.adapter.UserAdapter;
import com.c17206413.payup.ui.model.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.stripe.android.PaymentConfiguration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.c17206413.payup.MainActivity.currentUser;

public class CreatePaymentActivity extends AppCompatActivity implements UserAdapter.UserListener {

    private static final String TAG = "PAYMENT_CREATION";

    //firebase db
    private FirebaseFirestore db;

    //user recycler
    private RecyclerView searchRecycler;

    //user adapter for recycler
    private UserAdapter userAdapter;

    //list of all user
    private List<User> mUsers;

    //list of users added to payment
    private List<User> addedUsers;

    //UI elements
    private ProgressBar progressBar;
    private TextInputLayout nameInput;
    private TextInputLayout priceInput;
    private TextView pricePP;

    //variables
    private String current = "";
    private double perPerson;

    //locale for currency
    private Locale locale;

    //this indicates wether the user is included in the indicated price
    private int includes = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_payment);

        //initialise db
        db = FirebaseFirestore.getInstance();

        //get locale of current user
        locale = Locale.getDefault();

        //recycler of users
        searchRecycler = findViewById(R.id.user_recycler);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));

        //list all objects
        mUsers = new ArrayList<>();

        //list of users added to payment
        addedUsers = new ArrayList<>();

        //UI elements
        priceInput = findViewById(R.id.priceLayout);
        nameInput = findViewById(R.id.serviceNameLayout);
        pricePP = findViewById(R.id.price_per_person);
        progressBar = findViewById(R.id.progressBar1);

        //back button onClick
        ImageButton backButton= findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        //Check box to indicated if user is included in price
        CheckBox included = findViewById(R.id.checkBox);
        //if checkbox is changed format price accordingly
        included.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                includes = 1;
            } else {
                includes = 0;
            }
            formatPrice();
        });

        //set popup keyboard on price input to keypad
        Objects.requireNonNull(priceInput.getEditText()).setRawInputType(Configuration.KEYBOARD_12KEY);
        //Text change listener
        Objects.requireNonNull(priceInput.getEditText()).addTextChangedListener(new TextWatcher(){
            @Override
            //not required
            public void afterTextChanged(Editable s) {}

            @Override
            //not required
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            //format the new price
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)) {
                    formatPrice();
                }
            }
        });

        //Create the group payment with selected name and local currency
        Button createPaymentButton= findViewById(R.id.create_payment_button);
        createPaymentButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String serviceName = Objects.requireNonNull(nameInput.getEditText()).getText().toString();
            Currency currency =  Currency.getInstance(locale);
            createGroupPayment(serviceName, currency);

        });

        MainActivity.checkInternetConnection(this);

        //refresh users
        readUsers();
    }

    //Creates the individual document sfor each user
    private void createGroupPayment(String serviceName, Currency currency) {
        if (!validateNameForm(serviceName)) {
            return;
        }
        //get prcie per person
        long price = Math.round((perPerson / (addedUsers.size() + includes)));

        //check for valid price
        if (price >= 50 && addedUsers.size() != 0) {
            //for each of the added user
            for (int i = 0; i < addedUsers.size(); i++) {
                User user = addedUsers.get(i);
                String uid = user.getId();
                String name = user.getUsername();
                String amount = String.valueOf(price);

                //get current dateTime
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy\nHH:mm z");
                String currentDateAndTime = sdf.format(new Date());

                //create the payment data for teh document
                Map<String, Object> paymentDetails = new HashMap<>();
                paymentDetails.put("user_id", uid);
                paymentDetails.put("user_name", name);
                paymentDetails.put("currency", currency.getCurrencyCode());
                paymentDetails.put("amount", amount);
                paymentDetails.put("service_name", serviceName);
                paymentDetails.put("active", true);
                paymentDetails.put("date_created", currentDateAndTime);

                //add document to current users incoming collection
                db.collection("users").document(currentUser.getId()).collection("incoming")
                        .document()
                        .set(paymentDetails)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            }
            //once all documents are created end activity
            finish();
        } else {
            //popup to indicate invalid price
            Snackbar.make(findViewById(android.R.id.content), "Price per person must be at least 0.50 and at least one user must be selected.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    //used to search for all users
    private void readUsers() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //clear the users list
                        mUsers.clear();
                        //for each user document get the required info
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String id = document.getId();
                            String username = document.getString("name");
                            String profile = document.getString("profileUrl");
                            Uri profileUrl = null;
                            //only parse Uri if a profile picture has been set
                            if (profile != null) {
                                profileUrl = Uri.parse(profile);
                            }
                            User user = new User(id, username, profileUrl);
                            //if the user is the same as the current user, it should not be displayed
                            assert firebaseUser != null;
                            if (!user.getId().equals(firebaseUser.getUid())) {
                                mUsers.add(user);
                            }
                        }
                        //initialise user adapter with user list
                        userAdapter = new UserAdapter(this, mUsers, this);
                        //set that adapter to recycler
                        searchRecycler.setAdapter(userAdapter);
                    } else {
                        //set error
                        Log.w(TAG, "Error receiving document");
                        //popup for failed document received
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
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            nameInput.setError(null);
        }
        return valid;
    }

    @Override
    //when user is clicked, swap its isSelected
    public void onUserClick(int position) {
        mUsers.get(position).swapSelected();
        userAdapter = new UserAdapter(this, mUsers, this);
        searchRecycler.setAdapter(userAdapter);
        //used to reset the users added list with newly added users
        countSelected();
    }

    //counts the amount of users selected sets each users to the correct list
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
        //format price to correct value
        pricePP.setText(String.format("%s%s", getResources().getString(R.string.pricePerP), NumberFormat.getCurrencyInstance().format((perPerson/ 100 / (addedUsers.size() + includes)))));
    }

    private void formatPrice() {
        //get price
        String s = Objects.requireNonNull(priceInput.getEditText()).getText().toString();
        String cleanString = s.replaceAll("[$,£€.]", "");

        //parse string
        double parsed = Double.parseDouble(cleanString);
        perPerson = parsed;

        //convert to selected currency
        pricePP.setText(String.format("%s%s", getResources().getString(R.string.pricePerP), NumberFormat.getCurrencyInstance().format((perPerson/ 100 / (addedUsers.size() + includes)))));

        String formatted = NumberFormat.getCurrencyInstance().format((parsed/100));

        //set the new formated value
        current = formatted;
        priceInput.getEditText().setText(formatted);
        priceInput.getEditText().setSelection(formatted.length());
    }
}
