package com.zing.zalo.fillrammemory.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.zing.zalo.fillrammemory.callback.CoreCallback
import com.zing.zalo.fillrammemory.R
import com.zing.zalo.fillrammemory.utils.MemoryUtils
import kotlinx.android.synthetic.main.item_allocated.view.*
import kotlin.collections.ArrayList

class CreatedVarAdapter(
    private var mData: ArrayList<Int> = ArrayList(),
    private var selectedPos: Int = -1,
    private var lastSelectedCheckbox: CheckBox? = null,
    val callback: CoreCallback.With<Int>
): RecyclerView.Adapter<CreatedVarAdapter.CreatedVarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatedVarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_allocated, parent, false)
        return CreatedVarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CreatedVarViewHolder, position: Int) {
        holder.bind(mData[position], position)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun setData(data: ArrayList<Int>){
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    inner class CreatedVarViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Int, index: Int){
            view.variableIndex.text = "Variable ${index+1}"
            view.size.text = "Size: ${MemoryUtils.formatToString(item.toLong())}"
            view.checkbox.isChecked = index == selectedPos

            view.checkbox.setOnClickListener{
                if (index != selectedPos) {
                    selectedPos = index
                    callback.run(mData[index])
                    lastSelectedCheckbox?.isChecked = false
                    lastSelectedCheckbox = view.checkbox
                }
            }
        }
    }

}