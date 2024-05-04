package com.example.photoeditor.ui.dashboard.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.photoeditor.databinding.ItemFilterBinding
import com.example.photoeditor.ui.dashboard.model.FilterEntity

class FilterAdapter : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {
    var data: List<FilterEntity> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class FilterViewHolder(val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFilterBinding.inflate(inflater, parent, false)

        return FilterViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size // Количество элементов в списке данных

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = data[position] // Получение фильтра из списка данных по позиции

        with(holder.binding) {
            nameTextView.text = filter.text // Отрисовка названия
            imageView.setImageResource(filter.image)
        }
    }
}