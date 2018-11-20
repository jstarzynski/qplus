package com.quilmes.qplus.ui

import android.Manifest.permission.*
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.elstatgroup.elstat.sdk.errror.NexoError
import com.quilmes.qplus.R
import com.quilmes.qplus.viewmodel.StoreListViewModel
import kotlinx.android.synthetic.main.store_list_activity.*

class StoreListActivity : AppCompatActivity() {

    private val permissionsRequestCode = 666
    private lateinit var viewModel: StoreListViewModel
    private var progressDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.store_list_activity)
        initViews()
        initViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.store_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
            if (item?.itemId == R.id.action_get_bluetooth_log) {
                viewModel.authenticationStream.value?.getResult()?.let {
                    viewModel.generateUriForLog(this, it)
                    showProgress()
                }
                true
            } else
                super.onOptionsItemSelected(item)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permissionsRequestCode) {
            if (permissions.isNotEmpty()) {

                var allPermissionsGranted = true

                permissions.forEachIndexed { index, _ -> if (grantResults[index] == PackageManager.PERMISSION_DENIED) allPermissionsGranted = false }

                when {
                    allPermissionsGranted -> viewModel.retryAuthentication()
                    else -> assertPermissions()
                }

            } else TODO()

        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initViews() {
        val adapter = StoreListAdapter()

        storesList.layoutManager = LinearLayoutManager(this)
        storesList.adapter = adapter

        checkInButton.setOnClickListener {
            adapter.getSelectedStoreId()?.let {
                val intent = Intent(this, StoreActivity::class.java)
                viewModel.authenticationStream.value?.getResult()?.let { intent.putExtra(StoreActivity.EXTRA_AUTH_USER, it.userName) }
                intent.putExtra(StoreActivity.EXTRA_STORE_ID, it)
                startActivity(intent)
            } ?: showErrorMessage(R.string.error_msg_select_store_id)
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(StoreListViewModel::class.java)

        if (viewModel.authenticationStream.value?.isSuccessful() == true) switchProgress(true)
        else {
            switchProgress(false)
            viewModel.authenticationStream.observe(this, Observer {
                it?.let {
                    switchProgress(it.isSuccessful())
                    if (!it.isSuccessful()) {
                        if (it.getError().errorType == NexoError.NexoErrorType.SECURITY_ERROR)
                            assertPermissions()
                        else
                            showErrorMessage(R.string.error_msg_auth_error)
                    }
                }
            })

            viewModel.getLogUriStream().observe(this, Observer {
                hideProgress()
                if (it?.isSuccessful() == true) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, it.getResult())
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.msg_send_bt_log)))
                }
            })
        }
    }

    private fun assertPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val requiredPermissions = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE)
            val missingPermissions = mutableListOf<String>()
            requiredPermissions.forEach { if (checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) missingPermissions.add(it) }
            if (missingPermissions.size > 0)
                requestPermissions(missingPermissions.toTypedArray(), permissionsRequestCode)
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

    private fun showProgress() {
        if (progressDialog == null)
            progressDialog = MaterialDialog.Builder(this)
                    .title("Please wait")
                    .content("Operation in progress")
                    .progress(true, 0)
                    .cancelable(false)
                    .show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
        progressDialog = null
    }

}