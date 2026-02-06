package com.example.bloom_russo.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.R

class RemindersFragment : Fragment() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Notifications Enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            // Se negato, spegni gli switch visualmente
            view?.findViewById<SwitchCompat>(R.id.switchStart)?.isChecked = false
            view?.findViewById<SwitchCompat>(R.id.switchEnd)?.isChecked = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reminders, container, false)

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        val switchStart = view.findViewById<SwitchCompat>(R.id.switchStart)
        val switchEnd = view.findViewById<SwitchCompat>(R.id.switchEnd)

        // 1. CARICA STATO SALVATO (Persistence)
        val prefs: SharedPreferences = requireActivity().getSharedPreferences("bloom_prefs", Context.MODE_PRIVATE)

        val isStartEnabled = prefs.getBoolean("reminder_start", false)
        val isEndEnabled = prefs.getBoolean("reminder_end", false) // Nuova chiave

        switchStart.isChecked = isStartEnabled
        switchEnd.isChecked = isEndEnabled

        // 2. LISTENER PER "PERIOD STARTS"
        switchStart.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                prefs.edit().putBoolean("reminder_start", isChecked).apply()
                handleNotificationLogic(isChecked, "Your period is expected soon!")
            }
        }

        // 3. LISTENER PER "PERIOD ENDS" (Mancava questo salvataggio!)
        switchEnd.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                prefs.edit().putBoolean("reminder_end", isChecked).apply()
                handleNotificationLogic(isChecked, "Don't forget to log if your period ended!")
            }
        }

        return view
    }

    private fun handleNotificationLogic(isChecked: Boolean, message: String) {
        if (isChecked) {
            checkAndRequestPermission(message)
        } else {
            // Nota: In un'app reale dovresti cancellare l'allarme specifico per ID.
            // Qui cancelliamo genericamente per semplicità demo.
            cancelNotification(requireContext())
            Toast.makeText(context, "Reminder Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermission(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    scheduleNotification(requireContext(), message)
                    Toast.makeText(context, "Reminder Set!", Toast.LENGTH_SHORT).show()
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleNotification(requireContext(), message)
            Toast.makeText(context, "Reminder Set!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNotification(context: Context, message: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("message", message)
        }
        // Usiamo un requestCode diverso (0 o 1) se volessimo gestire notifiche multiple distinte
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)
                return
            }
        }
        // Test: Notifica tra 5 secondi
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)
    }

    private fun cancelNotification(context: Context) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}