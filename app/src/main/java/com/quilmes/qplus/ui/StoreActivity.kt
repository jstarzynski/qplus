package com.quilmes.qplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.quilmes.qplus.R
import com.quilmes.qplus.model.NexoStoreCoolerStatus
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

        storeCoolersInfo.setOnClickListener {
            startActivity(Intent(this, CoolerListActivity::class.java)
                            .apply { putExtra(EXTRA_STORE_ID, storeIdValue) })
        }

        checkOutButton.setOnClickListener {

            viewModel.coolers(storeIdValue).value?.also {
                val synced = it.count { it.status == NexoStoreCoolerStatus.SYNCED }
                val decommissioned = it.count { it.status == NexoStoreCoolerStatus.REQUIRES_COMMISSIONING }
                val unsynced = it.size - decommissioned - synced

                val summary = StringBuilder()

                if (unsynced > 0)
                    summary.append(getString(R.string.summary_unsynced_stub, unsynced)).append('\n')
                if (decommissioned > 0)
                    summary.append(getString(R.string.summary_decommissioned_stub, decommissioned)).append('\n')
                if (synced > 0)
                    summary.append(getString(R.string.summary_synced_stub, synced)).append('\n')

                MaterialDialog.Builder(this)
                        .title(getString(R.string.title_summary))
                        .content(summary.toString())
                        .positiveText(R.string.button_check_out)
                        .onPositive { _, _ ->
                            viewModel.checkOut(storeIdValue)
                            finish()
                        }
                        .show()
            }

        }
    }

    override fun onBackPressed() {}

}