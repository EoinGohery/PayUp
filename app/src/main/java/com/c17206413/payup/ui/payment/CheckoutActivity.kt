package com.c17206413.payup.ui.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.c17206413.payup.MainActivity
import com.c17206413.payup.R
import com.c17206413.payup.databinding.ActivityCheckoutBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.*
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.c17206413.payup.MainActivity.currentUser

class CheckoutActivity : AppCompatActivity() {

    //String values
    private var docId = ""
    private var clientSecret = ""
    private var tag = "CHECKOUT"
    private var paymentMethod = "Card"

    //binding to allow for more fluid layout object referencing
    private lateinit var binding: ActivityCheckoutBinding

    //initialise stripe application
    private val stripe: Stripe by lazy { Stripe(applicationContext, getString(R.string.publish_key)) }

    //initialise payments client
    private val paymentsClient: PaymentsClient by lazy {
        Wallet.getPaymentsClient(
                this,
                Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //check if google pay is possible
        isReadyToPay()

        //get intent extras
        val extras = intent.extras
        clientSecret = extras?.getString("clientSecret")!!
        val serviceName = extras.getString("serviceName")
        val currency = extras.getString("currency")
        val amount = extras.getDouble("amount")
        docId = extras.getString("id")!!

        //format currency
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(currency)

        //set layout UI variables
        binding.amountIndicator.text = format.format(amount)
        binding.serviceNameCheckout.text = serviceName

        //set back button on click
        binding.backButton.setOnClickListener {
            finish()
        }

        //remove postal code form card input
        binding.cardInputWidget.postalCodeEnabled=false

        //set card pay button click listener
        binding.payButton.setOnClickListener {
            binding.payGoogleButton.isClickable = false
            binding.payButton.isClickable = false
            binding.progressBar.visibility = VISIBLE
            paymentMethod = "Card"
            confirmPayment()
        }

        //set GooglePay button click listener
        binding.payGoogleButton.setOnClickListener {
            binding.payGoogleButton.isClickable = false
            binding.payButton.isClickable = false
            binding.progressBar.visibility = VISIBLE
            payWithGoogle()
        }

        //This can be used for card payments in future. (See further development of report)
        binding.paymentmethod.setOnClickListener {
            // Create the customer session and kick start the payment flow
        }

        MainActivity.checkInternetConnection(this)
    }

    //checks if google pay is possible
    private fun isReadyToPay() {
        paymentsClient.isReadyToPay(createIsReadyToPayRequest())
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            binding.payGoogleButton.isEnabled = true
                            binding.payGoogleButton.isClickable = true
                        } else {
                            binding.payGoogleButton.isEnabled = false
                            binding.payGoogleButton.isClickable = false
                        }
                    } catch (exception: ApiException) {
                    }
                }
    }

    //creates the is IsReadyToPayRequest JSON object for confirming card payment
    private fun createIsReadyToPayRequest(): IsReadyToPayRequest {
        return IsReadyToPayRequest.fromJson(
                JSONObject()
                        .put("apiVersion", 2)
                        .put("apiVersionMinor", 0)
                        .put("allowedPaymentMethods",
                                JSONArray().put(
                                        JSONObject()
                                                .put("type", "CARD")
                                                .put("parameters",
                                                        JSONObject()
                                                                .put("allowedAuthMethods",
                                                                        JSONArray()
                                                                                .put("PAN_ONLY")
                                                                                .put("CRYPTOGRAM_3DS")
                                                                )
                                                                .put("allowedCardNetworks",
                                                                        JSONArray()
                                                                                .put("AMEX")
                                                                                .put("DISCOVER")
                                                                                .put("MASTERCARD")
                                                                                .put("VISA")
                                                                )
                                                )
                                )
                        ).toString()
        )
    }

    //creates the is IsReadyToPayRequest JSON object for confirming GooglePay payment
    private fun createPaymentDataRequest(): PaymentDataRequest {
        val cardPaymentMethod = JSONObject()
                .put("type", "CARD")
                .put(
                        "parameters",
                        JSONObject()
                                .put("allowedAuthMethods", JSONArray()
                                        .put("PAN_ONLY")
                                        .put("CRYPTOGRAM_3DS"))
                                .put("allowedCardNetworks",
                                        JSONArray()
                                                .put("AMEX")
                                                .put("DISCOVER")
                                                .put("MASTERCARD")
                                                .put("VISA"))

                                // require billing address
                                .put("billingAddressRequired", true)
                                .put(
                                        "billingAddressParameters",
                                        JSONObject()
                                                // require full billing address
                                                .put("format", "MIN")

                                                // require phone number
                                                .put("phoneNumberRequired", true)
                                )
                )
                .put(
                        "tokenizationSpecification",
                        GooglePayConfig(this).tokenizationSpecification
                )

        // create PaymentDataRequest
        val paymentDataRequest = JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0)
                .put("allowedPaymentMethods",
                        JSONArray().put(cardPaymentMethod))
                .put("transactionInfo", JSONObject()
                        .put("totalPrice", "10.00")
                        .put("totalPriceStatus", "FINAL")
                        .put("currencyCode", "USD")
                )
                .put("merchantInfo", JSONObject()
                        .put("merchantName", "Example Merchant"))

                // require email address
                .put("emailRequired", true)
                .toString()

        return PaymentDataRequest.fromJson(paymentDataRequest)
    }

    //create the google payment method and activate the onActivityResult
    private fun payWithGoogle() {
        AutoResolveHelper.resolveTask(
                // 1 used to indicate GooglePay
                paymentsClient.loadPaymentData(createPaymentDataRequest()), this@CheckoutActivity, 1)
    }

    private fun confirmPayment() {
        val params = binding.cardInputWidget.paymentMethodCreateParams
        if (params != null) {
            val confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, clientSecret)
            stripe.confirmPayment(this, confirmParams)
        }
    }

    //This method cannot be replaced with the new activity result method
    //At the time of development google pay api has not been updated
    //to the new activity result method
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val weakActivity = WeakReference<Activity>(this)

        when (requestCode) {
            //if GooglePay
            1 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (data != null) {
                            //launch GooglePay handle payment
                            paymentMethod = "GooglePay"
                            onGooglePayResult(data)
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        //display payment failed popup
                        displayAlert(weakActivity.get()!!, "Payment cancelled", "Please try again.", restartDemo = false)
                        binding.payGoogleButton.isClickable = false
                        binding.payButton.isClickable = false
                        binding.progressBar.visibility = INVISIBLE
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        // Log the status for debugging
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        Log.w(tag, status.toString())
                    }
                    else -> {
                        //log unidentified issue
                        Log.w(tag, "Unidentified issue")
                        binding.payGoogleButton.isClickable = false
                        binding.payButton.isClickable = false
                        binding.progressBar.visibility = INVISIBLE
                    }
                }
            }
            //if card payment or if Google payment has already been formatted with onGooglePayResult()
            else -> {
                // Handle the result of stripe.confirmPayment
                stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
                    override fun onSuccess(result: PaymentIntentResult) {
                        val paymentIntent = result.intent
                        val status = paymentIntent.status
                        if (status == StripeIntent.Status.Succeeded) {
                            //display payment succeeded popup
                            displayAlert(weakActivity.get()!!, "Payment succeeded", "Returning", restartDemo = true)
                            //get current datetime
                            @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("dd/MM/yyyy\nHH:mm z")
                            val currentDateAndTime = sdf.format(Date())

                            //initialise db
                            val db = FirebaseFirestore.getInstance()
                            //get user ref
                            val userRef = db.collection("users").document(currentUser.id).collection("due").document(docId)
                            //update date paid
                            userRef.update("date_paid", currentDateAndTime)
                                    .addOnSuccessListener { Log.d(tag, "DocumentSnapshot successfully updated!") }
                                    .addOnFailureListener { e: java.lang.Exception? -> Log.w(tag, "Error updating document", e) }
                            //update paid status
                            userRef.update("active", false)
                                    .addOnSuccessListener { Log.d(tag, "DocumentSnapshot successfully updated!") }
                                    .addOnFailureListener { e: java.lang.Exception? -> Log.w(tag, "Error updating document", e) }
                            //update payment method used
                            userRef.update("payment_method", paymentMethod)
                                    .addOnSuccessListener { Log.d(tag, "DocumentSnapshot successfully updated!") }
                                    .addOnFailureListener { e: java.lang.Exception? -> Log.w(tag, "Error updating document", e) }
                        } else {
                            //display payment failed popup
                            displayAlert(weakActivity.get()!!, "Payment failed", paymentIntent.lastPaymentError?.message
                                    ?: "")
                            binding.progressBar.visibility = INVISIBLE
                        }
                    }

                    override fun onError(e: Exception) {
                        //display payment failed popup
                        displayAlert(weakActivity.get()!!, "Payment failed", e.toString())
                        Log.w(tag, e.toString())
                        binding.progressBar.visibility = INVISIBLE
                    }
                })
            }
        }
    }

    private fun onGooglePayResult(data: Intent) {
        val paymentData = PaymentData.getFromIntent(data) ?: return
        val paymentMethodCreateParams =
                PaymentMethodCreateParams.createFromGooglePay(
                        JSONObject(paymentData.toJson())
                )

        // now use the `paymentMethodCreateParams` object to create a PaymentMethod
        stripe.createPaymentMethod(
                paymentMethodCreateParams,
                callback = object : ApiResultCallback<PaymentMethod> {
                    override fun onSuccess(result: PaymentMethod) {
                        val confirmParams = ConfirmPaymentIntentParams
                                .createWithPaymentMethodId(result.id!!, clientSecret)
                        stripe.confirmPayment(this@CheckoutActivity, confirmParams)
                    }

                    override fun onError(e: Exception) {
                    }
                }
        )


    }

    private fun displayAlert(
            activity: Activity,
            title: String,
            message: String,
            restartDemo: Boolean = false
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
            if (restartDemo) {
                val intent = Intent()
                intent.putExtra("paymentSuccess", true)
                setResult(RESULT_OK, intent)
                builder.setPositiveButton("Ok") { _, _ -> finish() }
            } else {
                builder.setPositiveButton("Ok", null)
            }
            builder.create().show()
        }
    }
}


