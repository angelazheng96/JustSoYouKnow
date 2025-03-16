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
import android.service.notification.NotificationListenerService
import android.text.TextUtils


fun isNotificationAccessEnabled(context: Context): Boolean {
    // Get the list of enabled notification listener services
    val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")

    // Check if your app's service is in the list of enabled listeners
    return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(context.packageName)
}


class MainActivity : ComponentActivity() {
    fun ensureNotificationAccess(context: Context) {
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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustSoYouKnowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
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