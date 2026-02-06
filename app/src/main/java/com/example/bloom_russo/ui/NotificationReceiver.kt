package com.example.bloom_russo.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.bloom_russo.MainActivity
import com.example.bloom_russo.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: "Bloom Reminder"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bloom_channel"

        // Creazione Canale (Obbligatorio Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Cycle Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bell) // Assicurati che ic_bell esista
            .setContentTitle("Bloom")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}