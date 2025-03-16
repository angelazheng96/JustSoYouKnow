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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import android.os.Handler
import android.os.Looper
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GlobalSwitchState {
    var isEnabled by mutableStateOf(true)
}

fun isReceiveNotificationAccessEnabled(context: Context): Boolean {
    // Get the list of enabled notification listener services
    val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")

    // Check if your app's service is in the list of enabled listeners
    return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(context.packageName)
}

fun isSendNotificationAccessEnabled(context: Context): Boolean {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // For Android Oreo and above, check if notifications are enabled for the app
        notificationManager.areNotificationsEnabled()
    } else {
        // For below Android Oreo, assume notifications are allowed
        true
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var notificationReceiver: BroadcastReceiver

    // Ensure that permission to receive notifications is enabled
    private fun ensureReceiveNotificationAccess(context: Context) {
        // Check if the notification access permission is enabled
        if (!isReceiveNotificationAccessEnabled(context)) {
            // If not enabled, show a dialog prompting the user to enable it
            AlertDialog.Builder(context)
                .setTitle("Enable Notification Access")
                .setMessage("JustSoYouKnow needs notification access to detect all your other lovely notifications.")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Ensure Settings opens in a new task
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()

        } else {
            // If already enabled, show a success dialog
            AlertDialog.Builder(context)
                .setTitle("You're all good!")
                .setMessage("You've already allowed JustSoYouKnow to access all your other lovely notifications.")
                .setPositiveButton("Okay!", null)
                .show()
        }
    }

    // Ensure that permission to send notifications is enabled
    fun ensureSendNotificationAccess(context: Context) {

        // Check if the notification access permission is enabled
        if (!isSendNotificationAccessEnabled(context)) {
            // Show a dialog prompting the user to enable the required setting
            AlertDialog.Builder(context)
                .setTitle("Enable Notification Access")
                .setMessage("JustSoYouKnow needs permission to send you friendly notifications.")
                .setPositiveButton("Open Settings") { _, _ ->
                    // Open the App Notifications Settings page
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()

        } else {
            // If already enabled, show a success dialog
            AlertDialog.Builder(context)
                .setTitle("You're all good!")
                .setMessage("You've already allowed JustSoYouKnow to send you friendly notifications.")
                .setPositiveButton("Okay!", null)
                .show()
        }
    }

    // Create button to enable permission to receive notifications
    @Composable
    fun ReceiveSetting(context: Context) {
        var buttonColor by remember { mutableStateOf(Color.Red) }
        var buttonMessage by remember { mutableStateOf("Please Enable Receive Notification Access") }
        val lifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    buttonColor = if (isReceiveNotificationAccessEnabled(context)) Color.Gray else Color.Red
                    buttonMessage = if (isReceiveNotificationAccessEnabled(context)) "Receive Notification Access Enabled" else "Please Enable Receive Notification Access"
                }
            })
        }

        Button(
            onClick = {ensureReceiveNotificationAccess(this)},
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text(text = buttonMessage)
        }
    }

    // Create button to enable permission to send notifications
    @Composable
    fun SendSetting(context: Context) {
        var buttonColor by remember { mutableStateOf(Color.Red) }
        var buttonMessage by remember { mutableStateOf("Please Enable Send Notification Access") }
        val lifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    buttonColor = if (isSendNotificationAccessEnabled(context)) Color.Gray else Color.Red
                    buttonMessage = if (isSendNotificationAccessEnabled(context)) "Send Notification Access Enabled" else "Please Enable Send Notification Access"
                }
            })
        }

        Button(
            onClick = {ensureSendNotificationAccess(this)},
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text(text = buttonMessage)
        }
    }

    // Run every time a new instance of the app is opened for the first time
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var notificationTitle by mutableStateOf("No Notification")
        var notificationText by mutableStateOf("")

        // Random timer that sends notifications about the time
        val handler = Handler(Looper.getMainLooper())
        val context: Context = this
        val runnable = object : Runnable {
            override fun run() {
                // Your task here
                val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                createNotification(context, "Just So You Know...", "it's "+ time+" right now!")

                // Schedule the next execution with a random delay
                val randomDelay = (0..18000).random() // Random delay up to 60 sec
                handler.postDelayed(this, randomDelay.toLong())
            }
        }
        handler.postDelayed(runnable, (0..18000).random().toLong())

        // Register BroadcastReceiver
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == MyNotificationListener.ACTION_NOTIFICATION_RECEIVED) {
                    val title = intent.getStringExtra("title") ?: "No Title"
                    val text = intent.getStringExtra("text") ?: "No Text"

                    // Update UI
                    notificationTitle = title
                    notificationText = text

                    //Create Notif
                    Handler(Looper.getMainLooper()).postDelayed({
                        writeCustomNotification(applicationContext, title, text)
                    }, 1000) // Delay of 2 seconds
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
                        Greeting(name = "User")
                        Spacer(modifier = Modifier.height(16.dp))
                        ReceiveSetting(applicationContext)
                        SendSetting(applicationContext)
                        Spacer(modifier = Modifier.height(16.dp))
                        OffSwitch()
                    }
                }
            }
        }
    }
}

// Greeting message at top of main page
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Creates notification to let user know about new notification
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

// Generate text for new notification
fun writeCustomNotification(context: Context, title: String, text: String) {
    val wordArray = arrayOf(
        "you just got a new notification! :)",
        "your notification bell is working! :D",
        "THIS IS IMPORTANT !!!!!!!!!                                                  I think...",
        "something happened :p",
        "you are so popular and cool and awesome",
        "you should be productive >:(",
        "you can't read the notification anymore                                    that kinda sucks for you :(",
        "apples are actually terrible fruit tbh",
        "the notification you got does not matter",
        "you should read faster",
        "you shouldn't check your phone so often",
        "you                                                                                          looked haha",
        "CONGRATULATIONS!!                                              you got a notification! :D"
    )

    if (title != "No Notification" && !title.startsWith("Just So You Know...") && title.isNotEmpty() && GlobalSwitchState.isEnabled) {
        createNotification(context, "Just So You Know...", wordArray[(0..<wordArray.size).random()])
    }
}

// Off switch to turn off notifications
@Composable
fun OffSwitch() {
    var switchCounter by remember { mutableStateOf(0) }
    var isEnabled by remember { mutableStateOf(true) }
    var denialMessage by remember { mutableStateOf("Notifications ON") }
    val coroutineScope = rememberCoroutineScope()

    val denialComments = listOf(
        "Are you sure about that?",
        "Hmmm...",
        "Umm... I don't think so.",
        "That was a misclick, right?",
        "I know you didn't mean that -u-",
        "Why would you do that?",
        "Whoops - corrected your mistake :)",
        "Should you really be doing that?",
        "I don't think you want to do that :]",
        """Sorry Dave, I can't do that.""",
        "[Sorry, request failed :p]",
        "Hahaha, funny prank!",
        "No <3",
        "Request Denied :D"
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = denialMessage) // Displays the current message

        Switch(
            checked = isEnabled,
            onCheckedChange = {
                if (switchCounter in 0..5) {
                    switchCounter++
                    isEnabled = false

                    // Briefly turn it on, then off after 1 second
                    coroutineScope.launch {
                        delay(500)
                        isEnabled = true
                        denialMessage = denialComments.random()
                    }
                } else if (switchCounter == -1) {
                    isEnabled = it
                    denialMessage = "Notifications ON"
                    switchCounter = 0
                    GlobalSwitchState.isEnabled = true
                } else {
                    isEnabled = false
                    denialMessage = "Fine. You win."
                    switchCounter = -1
                    GlobalSwitchState.isEnabled = false
                }
            }
        )
    }
}
