package com.molchanov.cats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.molchanov.cats.repository.CatsRepository
import com.molchanov.cats.ui.Decoration
import com.molchanov.cats.utils.*



class MainActivity : AppCompatActivity() {


    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        APP_ACTIVITY = this
        DECORATION = Decoration(R.dimen.rv_item_margin)
        REPOSITORY = CatsRepository()
        FAV_QUERY_OPTIONS = mapOf(
            "user_id" to USER_ID,
            "limit" to "10",
            "page" to "1"
        )

        setContentView(R.layout.activity_main)

        FAB = this.findViewById(R.id.fab)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.homeFragment, R.id.favoritesFragment, R.id.uploadedFragment))
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(bottomNavigation, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}