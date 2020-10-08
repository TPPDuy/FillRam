package com.example.fillrammemory.classes

import java.io.Serializable

data class Memory(var total: Double = 0.0, var available: Double = 0.0, var created: Double = 0.0, var availablePercent: Int = 0) : Serializable