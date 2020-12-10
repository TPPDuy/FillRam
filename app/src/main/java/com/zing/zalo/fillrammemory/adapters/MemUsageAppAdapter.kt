package com.zing.zalo.fillrammemory.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.zing.zalo.fillrammemory.R
import com.zing.zalo.fillrammemory.classes.AppInfo
import kotlinx.android.synthetic.main.item_app.view.*
import java.util.*
import kotlin.collections.ArrayList

class MemUsageAppAdapter(val context: Context, data: List<AppInfo>?): RecyclerView.Adapter<MemUsageAppAdapter.MemUsageAppViewHolder>() {

    private val mData: MutableLiveData<ArrayList<AppInfo>> = MutableLiveData(ArrayList())

    init {
        if(data != null)
        {
            mData.value!!.addAll(data)
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

    fun replaceData(items: ArrayList<AppInfo>) {
        mData.value = items
        notifyDataSetChanged()
    }

    fun getData(): MutableLiveData<ArrayList<AppInfo>>{
        return mData
    }

    inner class MemUsageAppViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun onBind(item: AppInfo, index: Int){
            itemView.iconApp.setImageDrawable(item.icon)
            itemView.appName.text = item.name
            itemView.memUsage.text = "${context.getString(R.string.str_last_used)}: ${convertMillisToString(item.lastTimeUsed)}"
        }

        private fun convertMillisToString(millis: Long): String{
            val date = Date(millis)
            return DateFormat.format("dd-MM-yyyy hh:mm:ss", date).toString()
        }
    }
}