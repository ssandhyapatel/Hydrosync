package com.hydrosync.mobile

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hydrosync.mobile.databinding.ActivityWellnessBinding
import com.hydrosync.mobile.wellness.DrinkLog
import com.hydrosync.mobile.wellness.WellnessStore
import com.hydrosync.mobile.wellness.WellnessViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WellnessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWellnessBinding
    private val vm: WellnessViewModel by viewModels()

    @Inject lateinit var store: WellnessStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWellnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initial toggle states
        binding.swMorning.isChecked = store.getSmartMorning()
        binding.btnEnableNudge.text = if (store.getDailyNudge()) "Disable Daily Nudge" else "Enable Daily Nudge"

        binding.btnLogDrink.setOnClickListener {
            vm.logDrink(500)
            vm.refreshLogs()
        }

        binding.btnSetTimer.setOnClickListener {
            vm.setTimer(15) // schedule 15 minutes
        }

        binding.btnEnableNudge.setOnClickListener {
            val new = !store.getDailyNudge()
            vm.enableDailyNudge(new)
            binding.btnEnableNudge.text = if (new) "Disable Daily Nudge" else "Enable Daily Nudge"
        }

        binding.swMorning.setOnCheckedChangeListener { _, isChecked ->
            vm.setSmartMorning(isChecked)
        }

        observeLogs()
    }

    private fun observeLogs() {
        lifecycleScope.launch {
            vm.logs.collectLatest { list ->
                renderLogs(list)
            }
        }
    }

    private fun renderLogs(list: List<DrinkLog>) {
        val container: LinearLayout = binding.llLogs
        container.removeAllViews()
        if (list.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No logs yet"
            container.addView(tv)
            return
        }
        list.forEach { l ->
            val tv = TextView(this)
            val time = android.text.format.DateFormat.format("dd MMM, hh:mm a", l.timestamp)
            tv.text = "$time — ${l.ml} ml"
            container.addView(tv)
        }
    }
}
