package com.inu.bar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DrivingHistoryViewAdapter(private val dataSet: ArrayList<String>): RecyclerView.Adapter<DrivingHistoryViewAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.driving_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val tvHistoryDate : TextView = view.findViewById(R.id.tvDrivingHistoryDate)

        fun bind(data:String) {
            tvHistoryDate.text = data
        }
    }
}