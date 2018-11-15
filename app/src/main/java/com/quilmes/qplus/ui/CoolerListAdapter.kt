package com.quilmes.qplus.ui

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.quilmes.qplus.R
import com.quilmes.qplus.model.NexoStoreCooler

class CoolerListAdapter : RecyclerView.Adapter<CoolerListAdapter.CoolerItemViewHolder>() {

    var items = listOf<NexoStoreCooler>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class CoolerItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val status: TextView = itemView.findViewById(R.id.status)
        val progress: TextView = itemView.findViewById(R.id.progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoolerItemViewHolder =
            CoolerItemViewHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.cooler_list_item, parent, false))

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: CoolerItemViewHolder, position: Int) {
        viewHolder.name.text = items[position].name
        viewHolder.status.text = items[position].status.name
        items[position].progress.also { viewHolder.progress.text = if (it > 0) "$it%" else ""  }
    }

}