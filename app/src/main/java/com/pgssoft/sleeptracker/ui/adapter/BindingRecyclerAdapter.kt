package com.pgssoft.sleeptracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class BindingRecyclerAdapter<ViewModel>(
    private val lifecycleOwner: LifecycleOwner,
    private val layout: Int,
    var items: ArrayList<ViewModel>) : RecyclerView.Adapter<BindingRecyclerAdapter.BindingHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context), layout, parent, false)
        return BindingHolder(binding)
    }

    override fun onViewRecycled(holder: BindingHolder) {
        holder.binding.lifecycleOwner = null
        holder.binding.setVariable(BR.viewModel, null)
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        holder.binding.setVariable(BR.viewModel, items[position])
        holder.binding.lifecycleOwner = lifecycleOwner
    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    override fun getItemCount() = items.size

    class BindingHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {}
}