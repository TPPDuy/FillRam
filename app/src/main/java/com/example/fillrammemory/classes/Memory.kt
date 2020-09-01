package com.example.fillrammemory.classes

import java.io.Serializable

data class Memory(var total: Long = 0, var available: Long = 0, var created: Long = 0, var availablePercent: Int = 0) : Serializable{
}