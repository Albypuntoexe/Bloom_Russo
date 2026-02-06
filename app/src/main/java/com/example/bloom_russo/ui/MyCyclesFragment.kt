package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom_russo.R

class MyCyclesFragment : Fragment() {

    private val viewModel: MyCyclesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_cycles, container, false)

        // Setup Back Button
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.cyclesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = MyCyclesAdapter()
        recyclerView.adapter = adapter

        viewModel.cyclesList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return view
    }
}