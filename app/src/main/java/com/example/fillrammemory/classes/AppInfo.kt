package com.example.fillrammemory.classes

import android.graphics.drawable.Drawable
import java.io.Serializable

data class AppInfo(var name: String, var icon: Drawable, var memoryUsage: Int) : Serializable {
}