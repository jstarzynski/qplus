package com.quilmes.qplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.quilmes.qplus.R
import com.quilmes.qplus.viewmodel.StoreListViewModel
import kotlinx.android.synthetic.main.store_list_activity.*

class StoreListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.store_list_activity)
        initViews()
        initViewModel()
    }

    private fun initViews() {
        val adapter = StoreListAdapter()

        storesList.layoutManager = LinearLayoutManager(this)
        storesList.adapter = adapter

        checkInButton.setOnClickListener {
            adapter.getSelectedStoreId()?.let {
                val intent = Intent(this, StoreActivity::class.java)
                intent.putExtra(StoreActivity.EXTRA_STORE_ID, it)
                startActivity(intent)
            } ?: showErrorMessage(R.string.error_msg_select_store_id)
        }
    }

    private fun initViewModel() {
        val viewModel = ViewModelProviders.of(this).get(StoreListViewModel::class.java)

        if (viewModel.authenticationStream.value?.isSuccessful() == true) switchProgress(true)
        else {
            switchProgress(false)
            viewModel.authenticationStream.observe(this, Observer {
                it?.let {
                    switchProgress(it.isSuccessful())
                    if (!it.isSuccessful()) showErrorMessage(R.string.error_msg_auth_error)
                }
            })
        }
    }

    private fun switchProgress(hide: Boolean) {
        content.visibility = if (hide) View.VISIBLE else View.GONE
        progressContent.visibility = if (hide) View.GONE else View.VISIBLE
    }

    private fun showErrorMessage(@StringRes resId: Int) {
        MaterialDialog.Builder(this)
                .title(R.string.title_error_default)
                .content(resId)
                .positiveText(android.R.string.ok)
                .show()
    }

}