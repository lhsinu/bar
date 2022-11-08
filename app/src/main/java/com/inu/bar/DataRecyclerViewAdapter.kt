package com.inu.bar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.inu.bar.base.CommonUtils
import com.inu.bar.databinding.ItemDataBinding
import com.inu.bar.db.BarEntity

class DataRecyclerViewAdapter(private val dataList: ArrayList<BarEntity>, private val listener : OnItemClickListener) : RecyclerView.Adapter<DataRecyclerViewAdapter.MyViewHolder>()  {
    inner class MyViewHolder(binding: ItemDataBinding) : RecyclerView.ViewHolder(binding.root) {

        val tv_title = binding.tvTitle

        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding : ItemDataBinding = ItemDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val accidentData = dataList[position]

        val commonUtils = CommonUtils()
        holder.tv_title.text = commonUtils.convertLongToTime(accidentData.accidentdate)

        holder.root.setOnClickListener {
            listener.onDataItemClick(position)
            false
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

