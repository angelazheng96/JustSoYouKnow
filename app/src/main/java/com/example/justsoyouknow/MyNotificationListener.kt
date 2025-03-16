package com.example.justsoyouknow

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MyNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
    }
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener Connected")
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification received from: ${sbn.packageName}")

        // Extract notification details
        val packageName = sbn.packageName
        val tickerText = sbn.notification.tickerText?.toString()
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "No Title"
        val text = extras.getString("android.text") ?: "No Text"

        Log.d(TAG, "Title: $title, Text: $text, Ticker: $tickerText")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification removed from: ${sbn.packageName}")
    }
}
