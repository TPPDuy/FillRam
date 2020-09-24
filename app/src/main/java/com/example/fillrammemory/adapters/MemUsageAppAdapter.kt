package com.example.fillrammemory.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.fillrammemory.R
import com.example.fillrammemory.classes.AppInfo
import com.example.fillrammemory.callback.CoreCallback
import kotlinx.android.synthetic.main.item_app.view.*
import java.util.*
import kotlin.collections.ArrayList

class MemUsageAppAdapter(val context: Context, data: List<AppInfo>?, val callBack: CoreCallback.WithPare<Int, AppInfo>): RecyclerView.Adapter<MemUsageAppAdapter.MemUsageAppViewHolder>() {

    private val mData: MutableLiveData<ArrayList<AppInfo>> = MutableLiveData(ArrayList())
    private val checkedCount: MutableLiveData<Int> = MutableLiveData(0)

    init {
        if(data != null)
        {
            mData.value!!.addAll(data)
            checkedCount.value = itemCount
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemUsageAppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        return MemUsageAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemUsageAppViewHolder, position: Int) {
        val item = mData.value!![position]
        holder.onBind(item, position)
    }

    override fun getItemCount(): Int {
        return mData.value!!.size
    }

    fun addListData(items: ArrayList<AppInfo>) {
        mData.value = items
        notifyDataSetChanged()
        checkedCount.value = itemCount
    }

    fun clearData(){
        mData.value!!.clear()
        mData.value = mData.value
        notifyDataSetChanged()
    }

    fun getData(): MutableLiveData<ArrayList<AppInfo>>{
        return mData
    }

    fun getCheckedCount(): MutableLiveData<Int>{
        return checkedCount
    }
    fun changeSelectedStateAll(isSelected: Boolean){
        for(item in mData.value!!){
            item.isSelected = isSelected
        }
        mData.value = mData.value
        if (isSelected) checkedCount.value = itemCount
        else checkedCount.value = 0
        notifyDataSetChanged()
    }

    inner class MemUsageAppViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun onBind(item: AppInfo, index: Int){
            itemView.iconApp.setImageDrawable(item.icon)
            itemView.appName.text = item.name
            itemView.memUsage.text = "${context.getString(R.string.str_last_used)}: ${convertMillisToString(item.lastTimeUsed)}"
            itemView.checkbox.isChecked = item.isSelected

            itemView.checkbox.setOnClickListener {
                callBack.run(index, item)
                if (itemView.checkbox.isChecked)
                    checkedCount.value = checkedCount.value?.plus(1)
                else
                    checkedCount.value = checkedCount.value?.minus(1)
            }
        }

        private fun convertMillisToString(millis: Long): String{
            val date = Date(millis)
            return DateFormat.format("dd-MM-yyyy hh:mm:ss", date).toString()
        }
    }
}