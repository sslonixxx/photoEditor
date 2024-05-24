package com.example.photoeditor.recyclerView.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.photoeditor.R
import com.example.photoeditor.databinding.ItemFilterBinding
import com.example.photoeditor.recyclerView.adapter.model.FilterEntity

class FilterAdapter(private val onButtonClick: (Int) -> Unit) :
    RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {
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

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = data[position]

        with(holder.binding) {
            val filterNameResId = when (position) {
                0 -> R.string.rotation
                1 -> R.string.color_filters
                2 -> R.string.scaling
                3 -> R.string.recognize
                4 -> R.string.retouching
                5 -> R.string.unsharp_mask
                6 -> R.string.vector_editor
                7 -> R.string.affine_transform
                8 -> R.string.cube3d
                else -> R.string.default_filter_name
            }
            val filterName = holder.itemView.context.getString(filterNameResId)
            nameTextView.text = filterName
            imageView.setImageResource(filter.image)
            root.setOnClickListener {
                onButtonClick(position)
            }
        }
    }
}