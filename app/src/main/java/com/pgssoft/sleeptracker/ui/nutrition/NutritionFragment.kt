package com.pgssoft.sleeptracker.ui.nutrition

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.pgssoft.sleeptracker.R
import com.pgssoft.sleeptracker.databinding.FragmentNutritionBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class NutritionFragment : Fragment() {
    val viewModel by viewModel<NutritionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNutritionBinding.inflate(layoutInflater) ?: return null
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.recyclerLifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_sync -> {
            viewModel.syncButtonClicked()
            true
        }
        R.id.action_add -> {
            viewModel.addButtonClicked()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
