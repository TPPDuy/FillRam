package com.example.fillrammemory.Classes

import android.os.Parcelable
import java.io.Serializable

data class Memory(var total: Long, var available: Long, var availablePercent: Int) : Serializable{
}