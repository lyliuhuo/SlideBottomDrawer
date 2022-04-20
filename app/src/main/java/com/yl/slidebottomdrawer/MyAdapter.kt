package com.yl.slidebottomdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MyAdapter : ListAdapter<Int, MyAdapter.MyViewHolder>(DiffCallback) {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var tvText:TextView = itemView.findViewById(R.id.tv_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvText.text = "$position------------"
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context,"$position 被点击了",Toast.LENGTH_LONG).show()
        }
    }
}

object DiffCallback : DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }
}