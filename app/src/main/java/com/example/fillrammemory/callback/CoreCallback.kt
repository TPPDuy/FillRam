package com.example.fillrammemory.callback

interface CoreCallback {
    interface With<T>{
        fun run(p: T)
    }
    interface WithPare<K,V>{
        fun run(p1: K, p2: V)
    }
}