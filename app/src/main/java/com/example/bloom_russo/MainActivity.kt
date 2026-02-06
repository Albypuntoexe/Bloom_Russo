package com.example.bloom_russo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        // LOGICA AVVIO: Controlla se l'utente esiste già nel DB
        val db = AppDatabase.getDatabase(this)
        val existingUser = db.userDao().getUserDataSync()

        if (existingUser != null) {
            // Utente esiste -> Vai alla HOME
            navGraph.setStartDestination(R.id.homeFragment)
        } else {
            // Utente nuovo -> Vai all'ONBOARDING
            navGraph.setStartDestination(R.id.onboardingPeriodFragment)
        }

        // Applica il grafo modificato
        navController.graph = navGraph

        binding.bottomNavigation.setupWithNavController(navController)

        // Nascondi BottomBar durante onboarding
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.onboardingPeriodFragment ||
                destination.id == R.id.onboardingCycleFragment ||
                destination.id == R.id.onboardingDateFragment) {
                binding.bottomNavigation.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }
        }
    }
}