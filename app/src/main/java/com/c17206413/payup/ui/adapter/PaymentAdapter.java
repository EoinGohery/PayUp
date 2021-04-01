package com.c17206413.payup.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.R;
import com.c17206413.payup.ui.model.Payment;

import java.text.NumberFormat;
import java.util.List;

//recycler adapter for displaying payment informatiion
public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private final Context mContext;
    //list of payment objects
    private final List<Payment> mPayments;
    //click listener for recycler adapter
    private final PaymentListener paymentListener;

    //adapter constructor
    public PaymentAdapter(Context mContext, List<Payment> mPayments, PaymentListener paymentListener) {
        this.mPayments = mPayments;
        this.mContext = mContext;
        this.paymentListener = paymentListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.payment_item, parent, false);
        return new ViewHolder(view, paymentListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //set specific payment information to each adapter object on view holder
        Payment paymentDetails = mPayments.get(position);
        holder.price.setText(NumberFormat.getCurrencyInstance().format(paymentDetails.getAmount()));
        holder.userName.setText(paymentDetails.getUsername());
        holder.serviceName.setText(paymentDetails.getServiceName());
        //if payment is due set text to correct values and set money in/out to correct logo
        if (paymentDetails.getType().equals("due")) {
            holder.payButton.setText(R.string.app_name);
            holder.indicator.setVisibility(View.INVISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_arrow_down);
        //if payment is incoming set text to correct values and set money in/out to correct logo
        } else if (paymentDetails.getType().equals("incoming")) {
            holder.payButton.setText(R.string.received);
            holder.indicator.setVisibility(View.INVISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_arrow_up);
        }
        //if payment is not active hide the pay button and display indicator
        if (!paymentDetails.getActive()) {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.payButton.setVisibility(View.GONE);
        }
    }

    @Override
    //get the total number of items in list
    public int getItemCount() {
        return mPayments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView serviceName;
        public TextView price;
        public TextView userName;
        public PaymentListener PaymentListener;
        public Button payButton;
        public ImageView indicator;

        public ViewHolder(View itemView, PaymentListener paymentListener) {
            super(itemView);

            //payment listener
            this.PaymentListener = paymentListener;

            //UI elements
            serviceName = itemView.findViewById(R.id.service_name);
            price = itemView.findViewById(R.id.price);
            userName = itemView.findViewById(R.id.user_id);
            indicator = itemView.findViewById(R.id.in_out_indicator);

            //On click listener for entire object
            itemView.setOnClickListener(this);

            //On click listener for pay button inside object
            payButton = itemView.findViewById(R.id.pay_button);
            payButton.setOnClickListener(v -> PaymentListener.payButtonOnClick(v, getAdapterPosition()));
        }

        @Override
        public void onClick(View v) {
            PaymentListener.onPaymentDetailsClick(getAdapterPosition());
        }
    }

    //payment listener interface (methods to be overwritten in fragment classes)
    public interface PaymentListener {
        void onPaymentDetailsClick(int position);
        void payButtonOnClick(View v, int adapterPosition);
    }
}
