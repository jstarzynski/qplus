package com.quilmes.qplus.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.quilmes.qplus.R

class StoreListAdapter : RecyclerView.Adapter<StoreListAdapter.StoreItemHolder>() {

    private val storeIdList = Array(20) { "Store-${it+1}-${'A'+it}${'B'+it%3}${'Z'-it}" }
    private var selectedPosition = -1

    class StoreItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storeId: TextView = itemView.findViewById(R.id.storeId)
        val selectedMark: ImageView = itemView.findViewById(R.id.selectedMark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreItemHolder {
        val holder = StoreItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.store_list_item, parent, false))
        holder.itemView.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
        }
        return holder
    }

    override fun getItemCount(): Int = storeIdList.size

    override fun onBindViewHolder(viewHolder: StoreItemHolder, position: Int) {
        viewHolder.storeId.text = storeIdList[position]
        viewHolder.selectedMark.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
    }

    fun getSelectedStoreId(): String? = if (selectedPosition == -1) null else storeIdList[selectedPosition]

}