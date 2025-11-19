package com.hydrosync.mobile.ui.ble

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hydrosync.mobile.databinding.ItemBleDeviceBinding

class BleDeviceAdapter(
    private val items: MutableList<BluetoothDevice>,
    private val onClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BleDeviceAdapter.VH>() {

    inner class VH(val binding: ItemBleDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val device = items[adapterPosition]
                onClick(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBleDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val device = items[position]
        holder.binding.tvDeviceName.text = device.name ?: "Unknown Device"
    }

    override fun getItemCount(): Int = items.size

    fun addDevice(device: BluetoothDevice) {
        if (!items.contains(device)) {
            items.add(device)
            notifyItemInserted(items.size - 1)
        }
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }
}
