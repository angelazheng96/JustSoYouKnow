package com.example.justsoyouknow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.justsoyouknow.ui.theme.JustSoYouKnowTheme
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat


fun isNotificationAccessEnabled(context: Context): Boolean {
    // Get the list of enabled notification listener services
    val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")

    // Check if your app's service is in the list of enabled listeners
    return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(context.packageName)
}


class MainActivity : ComponentActivity() {
    fun ensureRecieveNotificationAccess(context: Context) {
        if (!isNotificationAccessEnabled(context)) {
            // Show a dialog prompting the user to enable the required setting
            AlertDialog.Builder(context)
                .setTitle("Enable Notification Access")
                .setMessage("This app needs notification access to detect notifications.")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Ensure Settings opens in a new task
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    fun ensureSendNotificationAccess(context: Context) {
        if (!isNotificationEnabled(context)) {
            // Show a dialog prompting the user to enable the required setting
            AlertDialog.Builder(context)
                .setTitle("Enable Notification Access")
                .setMessage("This app needs permission to send notifications.")
                .setPositiveButton("Open Settings") { _, _ ->
                    // Open the App Notifications Settings page
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    fun isNotificationEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android Oreo and above, check if notifications are enabled for the app
            notificationManager.areNotificationsEnabled()
        } else {
            // For below Android Oreo, assume notifications are allowed
            true
        }
    }



    @Composable
    fun sendSetting(context: Context) {
        Button(onClick = { ensureSendNotificationAccess(this) }) {
            Text(text = "Enable sending notifications")
        }
    }
    @Composable
    fun recieveSetting(context: Context) {
        Button(onClick = { ensureRecieveNotificationAccess(this) }) {
            Text(text = "Enable receiving notifications")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustSoYouKnowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting(name = "Android")
                        Spacer(modifier = Modifier.height(16.dp))
                        MyButton(applicationContext)
                        sendSetting(applicationContext)
                        recieveSetting(applicationContext)
                    }
                }
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JustSoYouKnowTheme {
        Greeting("Android")
    }
}

fun createNotification(context: Context, title: String, message: String) {
    Log.d("NotificationTest", "Notif Called!")
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // For Android 8.0 (API level 26) and above, a notification channel is required.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Log.d("NotificationTest", "Notif Channel Called!")
        val channelId = "default_channel"
        val channelName = "Default Notifications"
        val channelDescription = "This is the default notification channel."

        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = channelDescription

        // Register the channel with the system
        notificationManager.createNotificationChannel(notificationChannel)
    }

    // Build the notification
    val notification = NotificationCompat.Builder(context, "default_channel")
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    Log.d("NotificationTest", "Notif Sent!")
    // Show the notification
    notificationManager.notify(1, notification) // 1 is the notification ID
}


@Composable
fun MyButton(context: Context) {
    Button(onClick = { createNotification(context, "Button Pressed", "You just pressed a button!") }) {
        Text(text = "Click Me")
    }
}


