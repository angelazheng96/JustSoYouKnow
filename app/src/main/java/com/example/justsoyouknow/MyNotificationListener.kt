package com.example.justsoyouknow

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MyNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        const val ACTION_NOTIFICATION_RECEIVED = "com.example.justsoyouknow.NOTIFICATION_RECEIVED"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    // Broadcast new notification
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "No Title"
        val text = extras.getString("android.text") ?: "No Text"

        // Send broadcast with notification data to YOUR app
        val intent = Intent(ACTION_NOTIFICATION_RECEIVED).apply {
            putExtra("packageName", packageName)
            putExtra("title", title)
            putExtra("text", text)
            setPackage(applicationContext.packageName) // Ensure it's sent within app
        }

        sendBroadcast(intent) // Send broadcast to registered receivers in app
    }

}
