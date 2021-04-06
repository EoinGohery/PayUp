package com.c17206413.payup.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.c17206413.payup.MainActivity;
import com.c17206413.payup.R;
import com.c17206413.payup.ui.adapter.PaymentAdapter;
import com.c17206413.payup.ui.model.Payment;
import com.c17206413.payup.ui.payment.PaymentDetailsActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.c17206413.payup.MainActivity.currentUser;

public class IncomingFragment extends Fragment implements PaymentAdapter.PaymentListener{

    private static final String TAG = "INCOMING";

    //firebase elements
    private FirebaseFirestore db;

    //UI elements
    private List<Payment> mPayments;
    private RecyclerView recycler;
    private SwipeRefreshLayout pullToRefresh;

    //payment adapter to list payments
    private PaymentAdapter paymentAdapter;

    //create a new incoming fragment to be called by SectionsPageAdapter
    public static IncomingFragment newInstance() {
        return new IncomingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment, container, false);

        //list of payments to display
        mPayments = new ArrayList<>();

        //db initialization
        db = FirebaseFirestore.getInstance();

        //incoming recycler initialization
        recycler = root.findViewById(R.id.paymentRecycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        //add a pull down to refresh
        pullToRefresh = root.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            pullToRefresh.setRefreshing(true);
            readPayments();
            pullToRefresh.setRefreshing(false);
        });

        readPayments();

        return root;
    }

    public void onResume() {
        super.onResume();
        readPayments();
    }

    private void readPayments() {
        //clear current payments list
        mPayments.clear();
        String uid = MainActivity.getUser().getId();
        //only search for payments if user has logged in
        if ( uid != null) {
            List<Payment> tempPayments = new ArrayList<>();
            //query to get objects that are incoming and unpaid
            db.collection("users").document(uid).collection("incoming")
                    .whereEqualTo("active", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                //get payment information
                                String serviceName = document.getString("service_name");
                                //convert currency string to currency instance
                                Currency currency = Currency.getInstance(document.getString("currency"));
                                String name = document.getString("user_name");
                                String clientSecret = document.getString("clientSecret");
                                //parse and format the amount
                                Double amount = Double.parseDouble(Objects.requireNonNull(document.getString("amount"))) / 100;
                                String id = document.getId();
                                String dateCreated = document.getString("date_created");
                                //create a payment object
                                Payment paymentDetails = new Payment(id, serviceName, currency, name, amount, clientSecret, "incoming", true, dateCreated, null, null);
                                //add payment object to payments list
                                tempPayments.add(paymentDetails);
                            }
                            //initialise payment adapter
                            paymentAdapter = new PaymentAdapter(getActivity(), tempPayments, this);
                            //provide payments list to payments adapter
                            recycler.setAdapter(paymentAdapter);
                            mPayments=tempPayments;
                        } else {
                            Log.d(TAG, "Failed to receive payments");
                            //getActivity() cannot be null
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Failed to receive payments incoming.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
        }
    }

    //when user selects, this method launches the details screen
    private void viewPayment(Payment paymentDetail) {
        Intent intent = new Intent(getActivity(), PaymentDetailsActivity.class);
        intent.putExtra("amount", paymentDetail.getAmount());
        intent.putExtra("serviceName", paymentDetail.getServiceName());
        intent.putExtra("active", paymentDetail.getActive());
        intent.putExtra("user", paymentDetail.getUsername());
        intent.putExtra("currency", paymentDetail.getCurrency().getCurrencyCode());
        intent.putExtra("dateCreated", paymentDetail.getDateCreated());
        intent.putExtra("datePaid", paymentDetail.getDatePaid());
        intent.putExtra("paymentMethod", paymentDetail.getPaymentMethod());
        paymentDetailScreenLauncher.launch(intent);
    }

    //activity results handler payment details screen
    ActivityResultLauncher<Intent> paymentDetailScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //refresh list
                    readPayments();
                }
            });

    @Override
    //onclick listener from adapter overwritten
    //when item in list is clicked
    public void onPaymentDetailsClick(int position) {
        Payment paymentDetail = mPayments.get(position);
        viewPayment(paymentDetail);
    }

    //onclick listener from adapter overwritten
    //when pay button on item is clicked
    //payment is marked as received
    @Override
    public void payButtonOnClick(View v, int adapterPosition) {
        //get selected payment
        Payment paymentDetail = mPayments.get(adapterPosition);

        //document reference
        DocumentReference userRef = db.collection("users").document( currentUser.getId()).collection("incoming").document(paymentDetail.getId());
        //update paid status
        userRef.update("active", false)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

        //update date paid
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy\nHH:mm z");
        String currentDateAndTime = sdf.format(new Date());
        userRef.update("date_paid", currentDateAndTime)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

        //mark payment as paid outside app
        userRef.update("payment_method", "Outside App")
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

        //refresh payments list
        readPayments();
    }

}