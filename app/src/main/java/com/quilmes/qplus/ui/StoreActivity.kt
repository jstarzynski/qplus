package com.quilmes.qplus.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.quilmes.qplus.R
import kotlinx.android.synthetic.main.store_activity.*

class StoreActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORE_ID = "extraStoreId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.store_activity)

        storeId.text = intent.extras.getString(EXTRA_STORE_ID)
    }

    override fun onBackPressed() {}

}