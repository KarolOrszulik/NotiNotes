package com.example.notinotes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private val CHANNEL_ID = "note_notifications_channel"
    private val CHANNEL_NAME = "NoteNotes note"
    private val CHANNEL_DESCRIPTION = "Channel for note notifications"
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 123

    private var notificationId = 0
    private var isSticky = false

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        titleEditText = findViewById(R.id.noteTitle)
        contentEditText = findViewById(R.id.noteContent)

        findViewById<Button>(R.id.addNoteButton).setOnClickListener {
            if (requestNotificationPermissionIfNeeded()) {
                isSticky = false
                postNote()
            }
        }

        findViewById<Button>(R.id.addStickyNoteButton).setOnClickListener {
            if (requestNotificationPermissionIfNeeded()) {
                isSticky = true
                postNote()
            }
        }
    }

    private fun postNote() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        titleEditText.text.clear()
        contentEditText.text.clear()

        sendNotification(title, content, isSticky)
    }

    private fun sendNotification(title: String, content: String, sticky: Boolean) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle(title.ifEmpty { "[Empty note]" })
            .setContentText(content)

        if (sticky) {
            // TODO: Add sticky notification
        }

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(notificationId++, builder.build())
            } catch (e: SecurityException) {
                Toast.makeText(this@MainActivity,
                    "Notification permission denied. Please enable in settings.",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                postNote()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission denied. Please enable in settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                return false
            }
        }
        return true
    }
}