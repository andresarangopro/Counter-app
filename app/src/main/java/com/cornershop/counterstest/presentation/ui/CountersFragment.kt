package com.cornershop.counterstest.presentation.ui

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornershop.counterstest.R
import com.cornershop.counterstest.databinding.FragmentCountersBinding
import com.cornershop.counterstest.presentation.adapter.CounterListViewAdapter
import com.cornershop.counterstest.presentation.adapter.CounterRecyclerViewAdapter
import com.cornershop.counterstest.presentation.dialogs.MessageDialog
import com.cornershop.counterstest.presentation.parcelable.CounterAdapter
import com.cornershop.counterstest.presentation.viewModels.CounterEvent
import com.cornershop.counterstest.presentation.viewModels.CounterNavigation
import com.cornershop.counterstest.presentation.viewModels.CountersViewModel
import com.cornershop.counterstest.presentation.viewModels.utils.Event
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.items_times_view.view.*
import javax.inject.Inject


@AndroidEntryPoint
class CountersFragment : Fragment() {

    private var _binding: FragmentCountersBinding? = null
    private val binding get() = _binding!!

    lateinit var counterAdapter:CounterRecyclerViewAdapter
    lateinit var counterListAdapter:CounterListViewAdapter

    private val viewModel:CountersViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun validateEvents(event: Event<CounterNavigation>?) {
        event?.getContentIfNotHandled()?.let { navigation ->
            when(navigation){
                is CounterNavigation.setLoaderState-> navigation.run {
                     when(state){
                         true -> binding.loader.visibility = View.VISIBLE
                         else -> binding.loader.visibility = View.GONE
                     }
                }

                is CounterNavigation.setCounterList->navigation.run{

                    //setupList(binding.recyclerView, listCounter)
                    setTimesAndItems(listCounter?.size,timesSum)
                }

                is CounterNavigation.updateCounterList ->navigation.run {
                   // counterAdapter.updateData(listCounter)
                    setTimesAndItems(listCounter?.size,timesSum)
                }

                is CounterNavigation.setSelectedItemState-> navigation.run{
                    setVisibilityTopSetup(View.VISIBLE,View.GONE)

                    binding.tvSelectedItems.text = "${items?.let {
                        resources.getString(R.string.n_selected,
                            it
                        )
                    }}"
                }

                is CounterNavigation.hideSelectedItemState->{
                    setVisibilityTopSetup(View.GONE,View.VISIBLE)
                }

                is CounterNavigation.hideSwipeLoaderSave->{
                    binding.srwCounterList.isRefreshing = false
                }

                is CounterNavigation.onErrorLoadingCounterList-> navigation.run{
                    showErrorNotCountersYet(title,message)
                }
                is CounterNavigation.onErrorLoadingCounterListNetork-> navigation.run{
                    binding.errMessage.title = title?.let { resources.getString(it) }
                    binding.errMessage.message = message?.let { resources.getString(it) }
                    binding.errMessage.setActionRetry {
                        viewModel.postEvent(CounterEvent.getListCounterInit)
                        binding.errMessage.hideAll()
                    }
                    binding.errMessage.setView()
                }
            }
        }
    }

    private fun showErrorNotCountersYet(title:Int?, message:Int?) {
        binding.errMessage.title = title?.let { resources.getString(it) }
        binding.errMessage.message = message?.let { resources.getString(it) }
        binding.errMessage.setView()
    }

    private fun setTimesAndItems(items:Int?,times:Int?) {
        if (items != null) {
            if(items > 0) {
                binding.viewTimesItems.visibility = View.VISIBLE
                binding.viewTimesItems.tvItems.text = resources.getString(R.string.n_items, items)
                binding.viewTimesItems.tvTimes.text = resources.getString(R.string.n_times, times)
            }else{
                binding.viewTimesItems.visibility = View.GONE
                showErrorNotCountersYet(R.string.no_counters, R.string.no_counters_phrase)
            }
        }
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
                viewModel.postEvent(CounterEvent.FilterCounter(str))
                return false
            }

        })

        setupListAdapter()

        binding.btnAddCounter.setOnClickListener {
            val action= CountersFragmentDirections.actionCountersFragmentToCreateCounterFragment()
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.countersFragment, true).build()
            findNavController().navigate(action, navOptions)
        }

        binding.ivDeleteCounter.setOnClickListener {
            var dialog = MessageDialog.Builder()
                .setMessage(getString(R.string.delete_x_question,viewModel.listSelectedCounterAdapter.value))
                .setPositiveButton(getString(R.string.delete),DialogInterface.OnClickListener { dialogInterface, i ->
                    viewModel.postEvent(CounterEvent.DeleteSelectedCounters)
                })
                .setNegativeButton(getString(R.string.cancel),DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                }).dialog(AlertDialog.Builder(requireContext())).build()

            dialog.show(getChildFragmentManager(),"")
        }

        binding.ivCancel.setOnClickListener {
            setVisibilityTopSetup(View.GONE,View.VISIBLE)
            counterAdapter.unselectAllCounters()
        }

        binding.srwCounterList.setOnRefreshListener {
            viewModel.postEvent(CounterEvent.getListCounterFromSwipe)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCountersBinding.inflate(inflater,container,false)
        binding.loader.visibility = View.GONE
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        return binding.root
    }


    private fun setupList(
        view: View?,
        counterList: List<CounterAdapter>?
    ) {
        with(view as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            counterAdapter = CounterRecyclerViewAdapter(counterList,
                {id->
                    viewModel.postEvent(CounterEvent.IncreaseCounter(id))},
                { id->
                    viewModel.postEvent(CounterEvent.DecreaseCounter(id))
                },
                {listCounter->
                    //viewModel.postEvent(CounterEvent.SelectCounters(listCounter))
                }
            )

            adapter = counterAdapter
        }
    }

    private fun setupListAdapter( ) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext()
            )
            counterListAdapter = CounterListViewAdapter(
                {id->
                    viewModel.postEvent(CounterEvent.IncreaseCounter(id))},
                { id->
                    viewModel.postEvent(CounterEvent.DecreaseCounter(id))
                },
                {counter->
                    viewModel.postEvent(CounterEvent.SelectCounters(counter))
                }
            )
            adapter = counterListAdapter
        }
        viewModel.listCounterAdapter.observe(this) { list -> counterListAdapter.submitList(list) }
    }

}