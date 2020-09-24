package com.example.fillrammemory.classes

import android.graphics.drawable.Drawable
import java.io.Serializable

data class AppInfo(var packageName: String, var name: String, var icon: Drawable, var lastTimeUsed: Long, var isSelected: Boolean = true) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (other !is AppInfo) return false
        return (packageName == other.packageName)
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}