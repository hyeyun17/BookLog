package com.example.booklog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.booklog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var lastStatusBarHeight = 0

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            lastStatusBarHeight = statusBarHeight

            binding.topBar.updatePadding(top = statusBarHeight)
            binding.bottomNavigationView.updatePadding(bottom = navBarHeight)

            val isHome = navController.currentDestination?.id == R.id.homeFragment
            binding.navHostFragment.updatePadding(top = if (isHome) statusBarHeight else 0)

            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemReselectedListener { item ->
            val rootDestinationId = when (item.itemId) {
                R.id.homeFragment -> R.id.homeFragment
                R.id.searchFragment -> R.id.searchFragment
                R.id.mapFragment -> R.id.mapFragment
                R.id.libraryFragment -> R.id.libraryFragment
                R.id.timelineFragment -> R.id.timelineFragment
                else -> null
            }

            rootDestinationId?.let { targetId ->
                if (navController.currentDestination?.id != targetId) {
                    navController.popBackStack(targetId, false)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            if (!navController.navigateUp()) {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        val rootDestinations = setOf(
            R.id.homeFragment,
            R.id.searchFragment,
            R.id.mapFragment,
            R.id.libraryFragment,
            R.id.timelineFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isHome = destination.id == R.id.homeFragment

            binding.topBar.visibility = if (isHome) View.GONE else View.VISIBLE
            binding.navHostFragment.updatePadding(top = if (isHome) lastStatusBarHeight else 0)
            windowInsetsController.isAppearanceLightStatusBars = isHome

            binding.topTitle.text = when (destination.id) {
                R.id.homeFragment -> ""
                R.id.searchFragment -> "검색"
                R.id.mapFragment -> "지도"
                R.id.libraryFragment -> "라이브러리"
                R.id.timelineFragment -> "타임라인"
                R.id.reviewWriteFragment -> "독후감 쓰기"
                R.id.reviewDetailFragment -> "독후감"
                else -> ""
            }

            if (destination.id in rootDestinations) {
                binding.btnBack.visibility = View.GONE
            } else {
                binding.btnBack.visibility = View.VISIBLE
            }
        }
    }
}
