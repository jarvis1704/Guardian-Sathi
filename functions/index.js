const functions = require("firebase-functions/v1");
const { initializeApp } = require("firebase-admin/app");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.sendNotification = functions.database
  .ref("/{uid}/notifications/{notifId}")
  .onCreate(async (snapshot, context) => {
    const { title, body } = snapshot.val();
    const uid = context.params.uid;

    const tokenSnap = await getDatabase()
      .ref(`/${uid}/device_token`)
      .once("value");

    const token = tokenSnap.val();
    if (!token || !title || !body) return null;

    await getMessaging().send({
      token: token,
      data: {
        title: String(title),
        body: String(body),
        type: "GENERAL",
      },
    });

    return snapshot.ref.remove();
  });