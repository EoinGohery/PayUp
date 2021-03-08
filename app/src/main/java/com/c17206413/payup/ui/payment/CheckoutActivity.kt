package com.c17206413.payup.ui.payment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.c17206413.payup.R
import com.google.firebase.auth.*
import com.google.gson.GsonBuilder
import com.stripe.android.*
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.StripeIntent
import kotlinx.android.synthetic.main.activity_checkout.*
import java.lang.ref.WeakReference
import java.text.NumberFormat
import java.util.*


class CheckoutActivity : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null
    private var docId = ""
    private val stripe: Stripe by lazy { Stripe(applicationContext, "pk_test_51HnPJaAXocUznruHqwf1wdNuZeIEEkX9ODwT0yuhtsv9nFPoghcpWbRLDcq3GU0k7g3RlPwCQGhCHVcMPe9nmoqB00JWK66tDF") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        currentUser = FirebaseAuth.getInstance().currentUser

        val extras = intent.extras
        val clientSecret = extras?.getString("clientSecret")
        val serviceName = extras?.getString("serviceName")
        val currency = extras?.getString("currency")
        val amount = extras?.getDouble("amount")
        docId = extras?.getString("id")!!

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
            confirmPayment(clientSecret!!)
        }

        paymentmethod.setOnClickListener {
            // Create the customer session and kick start the payment flow

        }
    }

    private fun confirmPayment(paymentIntentClientSecret: String) {
        val params = cardInputWidget.paymentMethodCreateParams
        if (params != null) {
            val confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret)
            stripe.confirmPayment(this, confirmParams)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val weakActivity = WeakReference<Activity>(this)

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                val status = paymentIntent.status
                if (status == StripeIntent.Status.Succeeded) {
                    displayAlert(weakActivity.get()!!, "Payment succeeded", "Returning", restartDemo = true)
                } else {
                    displayAlert(weakActivity.get()!!, "Payment failed", paymentIntent.lastPaymentError?.message ?: "")
                }
            }

            override fun onError(e: Exception) {
                displayAlert(weakActivity.get()!!, "Payment failed", e.toString())
            }
        })
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
                builder.setPositiveButton("Ok") { _, _ -> finish() }
                val intent = Intent()
                intent.putExtra("paymentSuccess", true)
                intent.putExtra("docId", docId)
                setResult(RESULT_OK, intent)
            } else {
                builder.setPositiveButton("Ok", null)
            }
            builder.create().show()
        }
    }
}


