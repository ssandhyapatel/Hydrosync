package com.hydrosync.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hydrosync.mobile.databinding.ActivityAlertsBinding
import com.hydrosync.mobile.ui.alerts.Alert
import com.hydrosync.mobile.ui.alerts.AlertsAdapter
import com.hydrosync.mobile.ui.alerts.AlertsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertsBinding
    private val vm: AlertsViewModel by viewModels()
    private lateinit var adapter: AlertsAdapter

    private var currentFilterIsAll = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupUI()
        observeViewModel()
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_alerts

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_alerts -> true
                R.id.navigation_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_trends -> {
                    startActivity(Intent(this, TrendsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        adapter = AlertsAdapter(mutableListOf()) { alert ->
            showDetailsDialog(alert)
            vm.markRead(alert.id)
        }

        binding.rvAlerts.layoutManager = LinearLayoutManager(this)
        binding.rvAlerts.adapter = adapter

        // Swipe to Delete Logic
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removedAlert = adapter.removeAt(position)
                if (removedAlert != null) {
                    vm.deleteAlert(removedAlert.id)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvAlerts)

        binding.tvAll.setOnClickListener {
            currentFilterIsAll = true
            updateTabs(isAll = true)
            refreshList()
        }

        binding.tvUrgent.setOnClickListener {
            currentFilterIsAll = false
            updateTabs(isAll = false)
            refreshList()
        }

        // Initial state
        updateTabs(isAll = true)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            vm.alerts.collectLatest {
                refreshList()
            }
        }
    }

    private fun refreshList() {
        val allAlerts = vm.alerts.value
        val listToShow = if (currentFilterIsAll) {
            allAlerts
        } else {
            vm.filterUrgent(allAlerts)
        }
        adapter.setAll(listToShow)
    }

    private fun updateTabs(isAll: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.brand_primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_tertiary)

        if (isAll) {
            binding.tvAll.setTextColor(activeColor)
            binding.tvUrgent.setTextColor(inactiveColor)
        } else {
            binding.tvAll.setTextColor(inactiveColor)
            binding.tvUrgent.setTextColor(activeColor)
        }
    }

    private fun showDetailsDialog(alert: Alert) {
        val title = "${alert.severity.name}: ${alert.title}"
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("${alert.message}\n\nTime: ${alert.timestamp}")
            .setPositiveButton("Close", null)
            .show()
    }
}