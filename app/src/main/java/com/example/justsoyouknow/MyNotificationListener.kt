package com.example.justsoyouknow

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.os.Handler
import android.os.Looper
class MyNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        const val ACTION_NOTIFICATION_RECEIVED = "com.example.justsoyouknow.NOTIFICATION_RECEIVED"
    }
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener Connected")
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "No Title"
        val text = extras.getString("android.text") ?: "No Text"

        Log.d(TAG, "Title: $title, Text: $text, From: $packageName")

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(ACTION_NOTIFICATION_RECEIVED).apply {
                putExtra("packageName", packageName)
                putExtra("title", title)
                putExtra("text", text)
                setPackage(applicationContext.packageName) // Ensures broadcast stays in your app
            }
            sendBroadcast(intent) // Delayed broadcast
            Log.d(TAG, "Broadcast sent after delay")
        }, 1000) // Delay of 2000ms (2 seconds)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification removed from: ${sbn.packageName}")
    }

}
