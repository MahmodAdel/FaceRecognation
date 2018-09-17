package com.example.mahmoudfcih.simpleblogapp.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by mahmoud on 4/19/2017.
 */

public class FirebaseService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("message recived from",remoteMessage.getFrom());
        Log.e("message content",remoteMessage.getData().get("message"));

    }
}
