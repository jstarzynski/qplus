package com.quilmes.qplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.quilmes.qplus.R
import com.quilmes.qplus.viewmodel.StoreViewModel
import kotlinx.android.synthetic.main.store_activity.*

class StoreActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORE_ID = "extraStoreId"
        const val EXTRA_AUTH_USER = "extraAuthUser"
    }

    private lateinit var viewModel: StoreViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.store_activity)

        val storeIdValue = intent?.extras?.getString(EXTRA_STORE_ID) ?: String()
        val authUser = NexoAuthenticatedUser(intent?.extras?.getString(EXTRA_AUTH_USER))

        storeId.text = storeIdValue
        storeCoolersInfo.text = getString(R.string.coolers_info_stub, 0)

        viewModel = ViewModelProviders.of(this).get(StoreViewModel::class.java)

        if (savedInstanceState == null)
            viewModel.checkIn(authUser, storeIdValue)

        viewModel.coolers(storeIdValue).observe(this, Observer {
            storeCoolersInfo.text = getString(R.string.coolers_info_stub, it?.size ?: 0)
        })

    }

    override fun onBackPressed() {}

}