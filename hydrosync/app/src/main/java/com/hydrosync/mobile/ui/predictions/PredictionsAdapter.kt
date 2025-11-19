package com.hydrosync.mobile.ui.predictions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hydrosync.mobile.databinding.ItemPredictionBinding
import com.hydrosync.mobile.data.PredictionEntity
import java.text.SimpleDateFormat
import java.util.*

class PredictionsAdapter(private val items: MutableList<PredictionEntity>) :
    RecyclerView.Adapter<PredictionsAdapter.VH>() {

    inner class VH(val b: ItemPredictionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.b.tvTime.text = readable(p.timestamp)
        holder.b.tvHydration.text = "Hydration: ${p.hydrationPct.toInt()}%"
        holder.b.tvFatigue.text = "Fatigue: ${p.fatiguePct.toInt()}%"
        holder.b.tvSource.text = "Source: ${p.source}"

        // Optional: Color code source
        if(p.source == "model") {
            holder.b.tvSource.setTextColor(android.graphics.Color.parseColor("#2E7DA8"))
        } else {
            holder.b.tvSource.setTextColor(android.graphics.Color.GRAY)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setAll(newList: List<PredictionEntity>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    private fun readable(ts: Long): String {
        return SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(ts))
    }
}