package com.hydrosync.mobile.ui.trends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hydrosync.mobile.databinding.ItemTrendLogBinding
import com.hydrosync.mobile.data.SensorReading
import java.text.SimpleDateFormat
import java.util.*

class TrendsLogAdapter(private val items: MutableList<SensorReading>) :
    RecyclerView.Adapter<TrendsLogAdapter.VH>() {

    inner class VH(val b: ItemTrendLogBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTrendLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.b.tvTime.text = readable(r.timestamp)
        holder.b.tvDetails.text = "HR: ${r.hr} bpm • GSR: ${r.gsr} • Temp: ${r.temp}°C"

        // Simple visual indicator
        if (r.hr > 100 || r.gsr < 200) {
            holder.b.tvStatus.text = "Alert"
            holder.b.tvStatus.setTextColor(android.graphics.Color.RED)
        } else {
            holder.b.tvStatus.text = "Normal"
            holder.b.tvStatus.setTextColor(android.graphics.Color.parseColor("#05A88C"))
        }
    }

    override fun getItemCount(): Int = items.size

    fun setAll(newList: List<SensorReading>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun getItems(): List<SensorReading> = items

    private fun readable(ts: Long): String {
        return try {
            val df = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            df.format(Date(ts))
        } catch (e: Exception) {
            "-"
        }
    }
}