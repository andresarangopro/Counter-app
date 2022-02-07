package com.cornershop.counterstest.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornershop.counterstest.R
import com.cornershop.counterstest.databinding.FragmentCountersBinding
import com.cornershop.counterstest.entities.Counter
import com.cornershop.counterstest.presentation.adapter.CounterRecyclerViewAdapter
import com.cornershop.counterstest.presentation.parcelable.CounterListAdapter
import com.cornershop.counterstest.presentation.utils.onQueryTextChanged
import com.cornershop.counterstest.presentation.viewModels.CounterViewModelFactory
import com.cornershop.counterstest.presentation.viewModels.CountersViewModel
import com.cornershop.counterstest.presentation.viewModels.Event
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_counters.view.*
import kotlinx.android.synthetic.main.layout_welcome_content.view.*
import javax.inject.Inject


@AndroidEntryPoint
class CountersFragment : Fragment() {

    private var _binding: FragmentCountersBinding? = null
    private val binding get() = _binding!!

    lateinit var counterAdapter:CounterRecyclerViewAdapter

    lateinit var viewModel: CountersViewModel

    @Inject
    lateinit var viewModelFactory: CounterViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun validateEvents(event: Event<CountersViewModel.CounterNavigation>?) {
        event?.getContentIfNotHandled()?.let { navigation ->
            when(navigation){
                is CountersViewModel.CounterNavigation.setLoaderState-> navigation.run {
                     when(state){
                         true -> binding.loader.visibility = View.VISIBLE
                         else -> binding.loader.visibility = View.GONE
                     }
                }

                is CountersViewModel.CounterNavigation.setCounterList->navigation.run{
                    setupList(binding.recyclerView, listCounter)
                    setTimesAndItems(listCounter?.size,timesSum)
                }

                is CountersViewModel.CounterNavigation.updateCounterList ->navigation.run {
                    counterAdapter.updateData(listCounter)
                    setTimesAndItems(listCounter?.size,timesSum)
                }

                is CountersViewModel.CounterNavigation.setSelectedItemState-> navigation.run{
                    setVisibilityTopSetup(View.VISIBLE,View.GONE)

                    binding.tvSelectedItems.text = "${items?.let {
                        resources.getString(R.string.n_selected,
                            it
                        )
                    }}"
                }

                is CountersViewModel.CounterNavigation.hideSelectedItemState->{
                    setVisibilityTopSetup(View.GONE,View.VISIBLE)
                }
            }
        }
    }

    private fun setTimesAndItems(items:Int?,times:Int?) {
        binding.tvItems.text = resources.getString(R.string.n_items, items)
        binding.tvTimes.text = resources.getString(R.string.n_times, times)
    }

    private fun setVisibilityTopSetup(clSelectVisibility:Int, searchViewVisibility: Int) {
        binding.clSelected.visibility = clSelectVisibility
        binding.searchView.visibility = searchViewVisibility
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(str: String?): Boolean {
                viewModel.postEvent(CountersViewModel.CounterEvent.FilterCounter(str))
                return false
            }

        })

        binding.btnAddCounter.setOnClickListener {
            val action= CountersFragmentDirections.actionCountersFragmentToCreateCounterFragment()
            findNavController().navigate(action)
        }

        binding.ivDeleteCounter.setOnClickListener {
            viewModel.postEvent(CountersViewModel.CounterEvent.DeleteSelectedCounters)
        }

        binding.ivCancel.setOnClickListener {
            setVisibilityTopSetup(View.GONE,View.VISIBLE)
            counterAdapter.unselectAllCounters()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCountersBinding.inflate(inflater,container,false)

        setupViewModel()
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(CountersViewModel::class.java)
    }

    private fun setupList(
        view: View?,
        playlists: List<CounterListAdapter>?
    ) {
        with(view as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            counterAdapter = CounterRecyclerViewAdapter(requireContext(),playlists,
                {id->
                    viewModel.postEvent(CountersViewModel.CounterEvent.IncreaseCounter(id))},
                { id->
                    viewModel.postEvent(CountersViewModel.CounterEvent.DecreaseCounter(id))
                },
                {listCounter->
                    viewModel.postEvent(CountersViewModel.CounterEvent.SelectCounters(listCounter))
                }
            )

            adapter = counterAdapter
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CountersFragment().apply { }
    }
}