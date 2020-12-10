package com.zing.zalo.fillrammemory.callback

interface CoreCallback {
    interface With<T>{
        fun run(t: T)
    }

    interface WithPair<T, V>{
        fun run(t: T, v: V)
    }
}