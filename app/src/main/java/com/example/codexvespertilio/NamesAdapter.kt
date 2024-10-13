package com.example.codexvespertilio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NamesAdapter(
    private var names: List<String>, // Changed from val to var
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<NamesAdapter.NameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return NameViewHolder(view)
    }

    override fun onBindViewHolder(holder: NameViewHolder, position: Int) {
        val name = names[position]
        holder.textView.text = name
        holder.textView.setOnClickListener {
            onClick(name)
        }
    }

    override fun getItemCount(): Int = names.size

    class NameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    fun updateNames(newNames: List<String>) {
        names = newNames
        notifyDataSetChanged()
    }
}