package com.c17206413.payup.ui.payment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.c17206413.payup.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.stripe.android.*
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import kotlinx.android.synthetic.main.activity_checkout.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.NumberFormat
import java.util.*


class CheckoutActivity : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null
    private var docId = ""
    private var clientSecret = ""

    private val stripe: Stripe by lazy { Stripe(applicationContext, "pk_test_51HnPJaAXocUznruHqwf1wdNuZeIEEkX9ODwT0yuhtsv9nFPoghcpWbRLDcq3GU0k7g3RlPwCQGhCHVcMPe9nmoqB00JWK66tDF") }
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
        setContentView(R.layout.activity_checkout)

        currentUser = FirebaseAuth.getInstance().currentUser

        isReadyToPay()

        val extras = intent.extras
        clientSecret = extras?.getString("clientSecret")!!
        val serviceName = extras.getString("serviceName")
        val currency = extras.getString("currency")
        val amount = extras.getDouble("amount")
        docId = extras.getString("id")!!

        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(currency)

        amount_indicator.text = format.format(amount)

        service_name_checkout.text = serviceName

        backButton.setOnClickListener {
            finish()
        }

        cardInputWidget.postalCodeEnabled=false

        payButton.setOnClickListener {
            confirmPayment()
        }

        payGoogleButton.setOnClickListener {
            payWithGoogle()
        }

        paymentmethod.setOnClickListener {
            // Create the customer session and kick start the payment flow
        }
    }

    private fun isReadyToPay() {
        paymentsClient.isReadyToPay(createIsReadyToPayRequest())
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            payGoogleButton.isEnabled = true
                            payGoogleButton.isClickable = true
                        } else {
                            payGoogleButton.isEnabled = false
                            payGoogleButton.isClickable = false
                        }
                    } catch (exception: ApiException) {
                    }
                }
    }

    /**
     * See https://developers.google.com/pay/api/android/reference/request-objects#example
     * for an example of the generated JSON.
     */
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
                        )
                        .toString()
        )
    }

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

    private fun payWithGoogle() {
        AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(createPaymentDataRequest()),
                this@CheckoutActivity,
                LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    companion object {
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 53
    }

    private fun confirmPayment() {
        val params = cardInputWidget.paymentMethodCreateParams
        if (params != null) {
            val confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, clientSecret)
            stripe.confirmPayment(this, confirmParams)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val weakActivity = WeakReference<Activity>(this)

        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (data != null) {
                            onGooglePayResult(data)
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        // Cancelled
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        // Log the status for debugging
                        // Generally there is no need to show an error to
                        // the user as the Google Payment API will do that
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                    }
                    else -> {
                        // Do nothing.
                    }
                }
            }
            else -> {
                // Handle the result of stripe.confirmPayment
                stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
                    override fun onSuccess(result: PaymentIntentResult) {
                        val paymentIntent = result.intent
                        val status = paymentIntent.status
                        if (status == StripeIntent.Status.Succeeded) {
                            displayAlert(weakActivity.get()!!, "Payment succeeded", "Returning", restartDemo = true)
                        } else {
                            displayAlert(weakActivity.get()!!, "Payment failed", paymentIntent.lastPaymentError?.message
                                    ?: "")
                        }
                    }

                    override fun onError(e: Exception) {
                        displayAlert(weakActivity.get()!!, "Payment failed", e.toString())
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
        val params = stripe.createPaymentMethod(
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
                intent.putExtra("docId", docId)
                setResult(RESULT_OK, intent)
                builder.setPositiveButton("Ok") { _, _ -> finish() }
            } else {
                builder.setPositiveButton("Ok", null)
            }
            builder.create().show()
        }
    }
}


