'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const { Logging } = require('@google-cloud/logging');
const logging = new Logging({
  projectId: process.env.GCLOUD_PROJECT,
});
const stripe = require('stripe')(functions.config().stripe.secret, {
  apiVersion: '2020-08-27',
});

/**
 * When a user is created, create a Stripe customer object for them.
 */
exports.createUserDocument = functions.auth.user().onCreate(async (user) => {
  const customer = await stripe.customers.create({
    email: user.email,
    metadata: { firebaseUID: user.uid },
  });

  await admin.firestore().collection('users').doc(user.uid).set({
    customer_id: customer.id,
    name: user.displayName,
  });
  return;
});

/**
 * Set up an ephemeral key.
 */
exports.createEphemeralKey = functions.https.onCall(async (data, context) => {
  // Checking that the user is authenticated.
  if (!context.auth) {
    // Throwing an HttpsError so that the client gets the error details.
    throw new functions.https.HttpsError(
      'failed-precondition',
      'The function must be called while authenticated!'
    );
  }
  const uid = context.auth.uid;
  try {
    if (!uid) throw new Error('Not authenticated!');
    // Get stripe customer id
    const customer = (
      await admin.firestore().collection('users').doc(uid).get()
    ).data().customer_id;
    const key = await stripe.ephemeralKeys.create(
      { customer },
      { apiVersion: data.api_version }
    );
    return key;
  } catch (error) {
    throw new functions.https.HttpsError('internal', error.message);
  }
});

exports.authorizeStripeAccount = functions.https.onCall(async (data, context) => {
  // Checking that the user is authenticated.
  if (!context.auth) {
    // Throwing an HttpsError so that the client gets the error details.
    throw new functions.https.HttpsError(
      'failed-precondition',
      'The function must be called while authenticated!'
    );
  }
  const uid = context.auth.uid;
  try {
    if (!uid) throw new Error('Not authenticated!');
    // Get stripe customer id
    const code = data.code;
    const response = await stripe.oauth.token({
      grant_type: 'authorization_code',
      code: code,
    });
    
    var connected_account_id = response.stripe_user_id;

    await admin.firestore().collection('users').doc(uid).update({
      connected_account_id: connected_account_id,
    });
    return "Complete";
  } catch (error) {
    throw new functions.https.HttpsError('internal', error.message);
  }
});

/**
 * When a payment document is written on the client, this function is triggered to create the PaymentIntent in Stripe.
 */
exports.createStripePayment = functions.firestore
  .document('users/{userId}/incoming/{pushId}')
  .onCreate(async (snap, context) => {
    const { amount, currency, user_id, service_name} = snap.data();
    const ownerId = context.params.userId;
    try {
      // Look up the Stripe customer id.
      const customer = (await snap.ref.parent.parent.get()).data().connected_account_id;
      // Create a charge using the pushId as the idempotency key to protect against double charges.
      const idempotencyKey = context.params.pushId;
      const payment = await stripe.paymentIntents.create(
        {
          payment_method_types: ['card'],
          amount: amount,
          currency: currency,
          metadata: {'service_name': service_name, 'userID' : user_id}
        }, {
          stripeAccount: customer,
          idempotencyKey: idempotencyKey, }
      );
      const clientSecret = payment.client_secret;
      // If the result is successful, write it back to the database.
      //await snap.ref.set(payment);

      await admin.firestore().collection('users').doc(user_id).collection('due').doc(idempotencyKey).set({
        service_name: service_name,
        amount: amount,
        clientSecret: clientSecret,
        user_id: ownerId,
        active: true
      });

    } catch (error) {
      // We want to capture errors and render them in a user-friendly way, while still logging an exception with StackDriver
      console.log(error);
      await snap.ref.set({ error: userFacingMessage(error) }, { merge: true });
      await reportError(error, { user: context.params.userId });
    }
  });

/**
 * When a user deletes their account, clean up after them.
 */
exports.cleanupUser = functions.auth.user().onDelete(async (user) => {
  const dbRef = admin.firestore().collection('users');
  const customer = (await dbRef.doc(user.uid).get()).data();
  await stripe.customers.del(customer.customer_id);
  // Delete the customers payments & payment methods in firestore.
  const snapshot = await dbRef
    .doc(user.uid)
    .collection('payment_methods')
    .get();
  snapshot.forEach((snap) => snap.ref.delete());
  await dbRef.doc(user.uid).delete();
  //delete the connected account attached to this user
  const connected_account_id = customer.connected_account_id
  await stripe.oauth.deauthorize({
    client_id: 'ca_IOBii6j7E2TGUDjLMBChG5j65L8sq8GN',
    stripe_user_id: connected_account_id,
  });
});

function reportError(err, context = {}) {
  // This is the name of the StackDriver log stream that will receive the log
  // entry. This name can be any valid log stream name, but must contain "err"
  // in order for the error to be picked up by StackDriver Error Reporting.
  const logName = 'errors';
  const log = logging.log(logName);

  const metadata = {
    resource: {
      type: 'cloud_function',
      labels: { function_name: process.env.FUNCTION_NAME },
    },
  };

  const errorEvent = {
    message: err.stack,
    serviceContext: {
      service: process.env.FUNCTION_NAME,
      resourceType: 'cloud_function',
    },
    context: context,
  };

  // Write the error log entry
  return new Promise((resolve, reject) => {
    log.write(log.entry(metadata, errorEvent), (error) => {
      if (error) {
        return reject(error);
      }
      return resolve();
    });
  });
}

/**
 * Sanitize the error message for the user.
 */
function userFacingMessage(error) {
  return error.type
    ? error.message
    : 'An error occurred, developers have been alerted';
}
