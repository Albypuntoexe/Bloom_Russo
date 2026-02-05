package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bloom_russo.databinding.FragmentCalendarBinding
import java.time.LocalDate

class CalendarFragment : Fragment() {

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var binding: FragmentCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        val adapter = CalendarAdapter(requireContext())
        val layoutManager = GridLayoutManager(context, 7) // 7 colonne per i giorni della settimana
        layoutManager.spanSizeLookup = adapter.spanSizeLookup // Gestisce gli header a larghezza piena

        binding.calendarRecyclerView.layoutManager = layoutManager
        binding.calendarRecyclerView.adapter = adapter

        // Osserva i dati
        viewModel.calendarItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            // Scrolla automaticamente al mese corrente (circa a metà lista)
            // In una implementazione reale cercheremmo l'indice esatto di "Oggi"
            binding.calendarRecyclerView.scrollToPosition(items.size / 2)
        }

        return binding.root
    }
}