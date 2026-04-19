package com.gallerymart.app.feature.order.ui.buyer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gallerymart.app.databinding.ActivityOrderListBinding
import com.gallerymart.app.feature.order.vm.OrderUiState
import com.gallerymart.app.feature.order.vm.OrderViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class OrderListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderListBinding
    private val viewModel: OrderViewModel = OrderViewModel()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        observeViewModel()

        viewModel.getMyOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter { order ->
            startActivity(OrderDetailActivity.newIntent(this, order.id))
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterOrders(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterOrders(position: Int) {
        val state = viewModel.listState.value.ordersState
        if (state is OrderUiState.Success) {
            val allOrders = state.data
            val filtered = when (position) {
                1 -> allOrders.filter { it.status.uppercase() == "CONFIRMED" || it.status.uppercase() == "DELIVERING" }
                2 -> allOrders.filter { it.status.uppercase() == "COMPLETED" || it.status.uppercase() == "SOLD" }
                3 -> allOrders.filter { it.status.uppercase() == "CANCELLED" }
                else -> allOrders
            }
            adapter.submitList(filtered)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.listState.collect { state ->
                    when (val ordersState = state.ordersState) {
                        is OrderUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is OrderUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            filterOrders(binding.tabLayout.selectedTabPosition)
                        }
                        is OrderUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@OrderListActivity, ordersState.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, OrderListActivity::class.java)
        }
    }
}
