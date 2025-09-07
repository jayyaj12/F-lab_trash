package com.aos.f_lab_trash.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aos.f_lab_trash.databinding.ItemListBinding
import com.aos.f_lab_trash.view.ui.theme.Item

class ListAdapter(
    private val onClickedRecoveryItem: (Item) -> Unit,
    private val onCanceledRecoveryItem: (Item) -> Unit
) : ListAdapter<Item, ListAdapter.ViewHolder>(ItemDiffCallback()) {

    // DiffUtil.ItemCallback 구현
    class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.ivDump.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClickedRecoveryItem(getItem(pos))
                }
            }
            binding.btnCancel.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onCanceledRecoveryItem(getItem(pos))
                }
            }
        }

        fun onBind(item: Item) {
            binding.tvName.text = item.name
            binding.tvCountdown.text = item.countdownSeconds?.let {
                "${it}초 후 복구"
            } ?: "대기중"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}