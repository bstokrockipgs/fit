package com.pgssoft.sleeptracker.ui.sleep

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.pgssoft.sleeptracker.R
import com.pgssoft.sleeptracker.databinding.FragmentSleepBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SleepFragment : Fragment() {
    val viewModel by viewModel<SleepViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSleepBinding.inflate(layoutInflater) ?: return null
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
