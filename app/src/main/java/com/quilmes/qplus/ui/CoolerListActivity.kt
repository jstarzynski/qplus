package com.quilmes.qplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.quilmes.qplus.R
import com.quilmes.qplus.viewmodel.StoreViewModel
import kotlinx.android.synthetic.main.cooler_list_activity.*

class CoolerListActivity : AppCompatActivity() {

    private lateinit var adapter: CoolerListAdapter
    private lateinit var viewModel: StoreViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cooler_list_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = CoolerListAdapter()
        coolersList.adapter = adapter
        coolersList.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProviders.of(this).get(StoreViewModel::class.java)

        viewModel.coolers(intent.getStringExtra(StoreActivity.EXTRA_STORE_ID))
                .observe(this, Observer { it?.apply { adapter.items = it } })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

}