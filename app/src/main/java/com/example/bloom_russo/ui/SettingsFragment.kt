package com.example.bloom_russo.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
        // Inizializza DAO
        dao = AppDatabase.getDatabase(requireContext()).userDao()

        // Back Button
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        // Edit Period Duration
        view.findViewById<LinearLayout>(R.id.btnEditPeriodDuration).setOnClickListener {
            showEditDialog("Period Duration", true)
        }

        // Edit Cycle Length
        view.findViewById<LinearLayout>(R.id.btnEditCycleLength).setOnClickListener {
            showEditDialog("Cycle Length", false)
        }

        // Delete All Data
        view.findViewById<TextView>(R.id.btnDeleteAll).setOnClickListener {
            showDeleteConfirmation()
        }

        return view
    }

    private fun showEditDialog(title: String, isPeriod: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val value = input.text.toString().toIntOrNull()
            if (value != null && value > 0) {
                lifecycleScope.launch {
                    val user = dao.getUserDataSync()
                    if (user != null) {
                        if (isPeriod) user.periodDuration = value
                        else user.cycleLength = value
                        dao.insertOrUpdate(user)
                        Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show()
                    }
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
            // Nuke tables
            dao.deleteAllUserData()
            dao.deleteAllPeriodDays()

            // Riavvia l'app
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Runtime.getRuntime().exit(0)
        }
    }
}