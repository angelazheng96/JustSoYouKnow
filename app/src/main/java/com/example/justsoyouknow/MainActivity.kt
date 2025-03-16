package com.example.justsoyouknow

import android.annotation.SuppressLint
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
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat


fun isNotificationAccessEnabled(context: Context): Boolean {
    // Get the list of enabled notification listener services
    val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")

    // Check if your app's service is in the list of enabled listeners
    return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(context.packageName)
}


class MainActivity : ComponentActivity() {
    private lateinit var notificationReceiver: BroadcastReceiver

    private fun ensureNotificationAccess(context: Context) {
        // Check if the notification access permission is enabled
        if (!isNotificationAccessEnabled(context)) {
            // If not enabled, show a dialog prompting the user to enable it
            AlertDialog.Builder(context)
                .setTitle("Enable Notification Access")
                .setMessage("This app needs notification access to detect notifications. Please enable it in settings.")
                .setPositiveButton("Open Settings") { _, _ ->
                    // Open the Notification Listener Settings
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var notificationTitle by mutableStateOf("No Notification")
        var notificationText by mutableStateOf("")

        // Register BroadcastReceiver
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == MyNotificationListener.ACTION_NOTIFICATION_RECEIVED) {
                    val title = intent.getStringExtra("title") ?: "No Title"
                    val text = intent.getStringExtra("text") ?: "No Text"

                    Log.d("MainActivity", "Received notification: $title - $text")

                    // Update UI
                    notificationTitle = title
                    notificationText = text

                    //Create Notif
                    writeCustomNotification(applicationContext, title, text)
                }
            }
        }

        val intentFilter = IntentFilter("com.example.justsoyouknow.NOTIFICATION_RECEIVED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, intentFilter)
        }

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
                    }
                }
            }
        }
        ensureNotificationAccess(this)
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
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // For Android 8.0 (API level 26) and above, a notification channel is required.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    // Show the notification
    notificationManager.notify(1, notification) // 1 is the notification ID
}

fun writeCustomNotification(context: Context, title: String, text: String) {
    if( !(title == "No Notification" || title.startsWith("JUST RECEIVED A NOTIFICATION") || title.isEmpty())){
        createNotification(context, "JUST RECEIVED A NOTIFICATION - $title", "TEXT - $text")
    }
}


@Composable
fun MyButton(context: Context) {
    Button(onClick = { createNotification(context, "Button Pressed", "You just pressed a button!") }) {
        Text(text = "Click Me")
    }
}
