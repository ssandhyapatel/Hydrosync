package com.hydrosync.mobile.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hydrosync.mobile.databinding.ItemRecentLogBinding
import com.hydrosync.mobile.data.SensorReading
import java.text.SimpleDateFormat
import java.util.*

class RecentLogAdapter(private val items: MutableList<SensorReading>) :
    RecyclerView.Adapter<RecentLogAdapter.VH>() {

    inner class VH(val b: ItemRecentLogBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRecentLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.b.tvTime.text = readable(r.timestamp)
        holder.b.tvValues.text = "HR: ${r.hr} • GSR: ${r.gsr} • T: ${r.temp}"
    }

    override fun getItemCount(): Int = items.size

    fun setAll(newList: List<SensorReading>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    private fun readable(ts: Long): String {
        return try {
            val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            df.format(Date(ts))
        } catch (e: Exception) {
            ""
        }
    }
}