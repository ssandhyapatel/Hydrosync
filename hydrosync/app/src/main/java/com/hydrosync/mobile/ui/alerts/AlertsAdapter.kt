package com.hydrosync.mobile.ui.alerts

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hydrosync.mobile.databinding.ItemAlertBinding
import java.text.SimpleDateFormat
import java.util.*

class AlertsAdapter(
    private val items: MutableList<Alert>,
    private val onViewClicked: (Alert) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.VH>() {

    inner class VH(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]

        // Capitalize severity
        holder.binding.tvSeverity.text = a.severity.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        holder.binding.tvMessage.text = a.message
        holder.binding.tvTime.text = readableTime(a.timestamp)

        // Dynamic Colors & Icons
        when (a.severity) {
            Severity.URGENT -> {
                holder.binding.ivSeverity.setColorFilter(Color.parseColor("#D32F2F")) // Red
                holder.binding.tvSeverity.setTextColor(Color.parseColor("#D32F2F"))
            }
            Severity.MILD -> {
                holder.binding.ivSeverity.setColorFilter(Color.parseColor("#F57C00")) // Orange
                holder.binding.tvSeverity.setTextColor(Color.parseColor("#F57C00"))
            }
            Severity.INFO -> {
                holder.binding.ivSeverity.setColorFilter(Color.parseColor("#05A88C")) // Teal
                holder.binding.tvSeverity.setTextColor(Color.parseColor("#05A88C"))
            }
        }

        // Opacity for Read/Unread
        holder.binding.root.alpha = if (a.read) 0.6f else 1.0f

        holder.binding.btnView.setOnClickListener { onViewClicked(a) }
    }

    override fun getItemCount(): Int = items.size

    fun setAll(newList: List<Alert>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int): Alert? {
        if (position in items.indices) {
            val removed = items.removeAt(position)
            notifyItemRemoved(position)
            return removed
        }
        return null
    }

    private fun readableTime(ts: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            sdf.format(Date(ts))
        } catch (e: Exception) {
            "-"
        }
    }
}
