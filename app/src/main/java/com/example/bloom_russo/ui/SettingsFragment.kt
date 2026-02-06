package com.example.bloom_russo.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker // IMPORTANTE
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.MainActivity
import com.example.bloom_russo.R
import com.example.bloom_russo.data.AppDatabase
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var dao: com.example.bloom_russo.data.UserDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        dao = AppDatabase.getDatabase(requireContext()).userDao()

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<LinearLayout>(R.id.btnEditPeriodDuration).setOnClickListener {
            showNumberPickerDialog("Period Duration", true)
        }

        view.findViewById<LinearLayout>(R.id.btnEditCycleLength).setOnClickListener {
            showNumberPickerDialog("Cycle Length", false)
        }

        view.findViewById<TextView>(R.id.btnDeleteAll).setOnClickListener {
            showDeleteConfirmation()
        }

        return view
    }

    private fun showNumberPickerDialog(title: String, isPeriod: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        // Creiamo il NumberPicker
        val numberPicker = NumberPicker(requireContext())

        // Impostiamo range sensati
        if (isPeriod) {
            numberPicker.minValue = 1
            numberPicker.maxValue = 15
            numberPicker.value = 5 // Default visuale
        } else {
            numberPicker.minValue = 15
            numberPicker.maxValue = 50
            numberPicker.value = 28 // Default visuale
        }

        // Recuperiamo il valore corrente dal DB per mostrarlo (opzionale ma carino)
        lifecycleScope.launch {
            val user = dao.getUserDataSync()
            if (user != null) {
                if (isPeriod) numberPicker.value = user.periodDuration
                else numberPicker.value = user.cycleLength
            }
        }

        // Aggiungiamo il picker al dialog
        builder.setView(numberPicker)

        builder.setPositiveButton("Save") { _, _ ->
            val value = numberPicker.value
            lifecycleScope.launch {
                val user = dao.getUserDataSync()
                if (user != null) {
                    if (isPeriod) user.periodDuration = value
                    else user.cycleLength = value
                    dao.insertOrUpdate(user)
                    Toast.makeText(context, "Updated to $value days!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Data")
            .setMessage("Are you sure? This will delete all your cycles and settings. The app will restart.")
            .setPositiveButton("Delete") { _, _ ->
                performDelete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete() {
        lifecycleScope.launch {
            dao.deleteAllUserData()
            dao.deleteAllPeriodDays()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Runtime.getRuntime().exit(0)
        }
    }
}