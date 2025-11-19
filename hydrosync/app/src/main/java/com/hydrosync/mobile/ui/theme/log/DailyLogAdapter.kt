package com.hydrosync.mobile.ui.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hydrosync.mobile.databinding.ItemLogEntryBinding

class DailyLogAdapter(private val items: List<String>) :
    RecyclerView.Adapter<DailyLogAdapter.LogVH>() {

    inner class LogVH(val binding: ItemLogEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogVH {
        val binding = ItemLogEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogVH(binding)
    }

    override fun onBindViewHolder(holder: LogVH, position: Int) {
        val text = items[position]
        holder.binding.tvLogTime.text = text
    }

    override fun getItemCount(): Int = items.size
}
