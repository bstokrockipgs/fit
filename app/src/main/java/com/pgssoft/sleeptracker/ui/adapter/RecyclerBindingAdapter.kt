package com.pgssoft.sleeptracker.ui.adapter

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("items", "itemLayout", "lifecycleOwner", requireAll = false)
fun <ViewModel> setRecyclerAdapter(view: RecyclerView,
                                   items: List<ViewModel>?,
                                   itemLayout: Int?,
                                   lifecycleOwner: LifecycleOwner) {
    if (items == null || itemLayout == null) return

    if (view.layoutManager == null) {
        view.layoutManager = LinearLayoutManager(view.context)
    }

    view.adapter = BindingRecyclerAdapter(lifecycleOwner, itemLayout, ArrayList(items))
}