package com.example.fillrammemory.classes

class VarHolder<T> {
    private var mAllocations = ArrayList<T>()

    fun addVar(allocation: T){
        synchronized(mAllocations){
            mAllocations.add(allocation)
        }
    }

    fun getIterator(): Iterator<T> {
        synchronized(mAllocations){
            return mAllocations.iterator()
        }
    }
    fun removeAt(index: Int){
        synchronized(mAllocations){
            if (index in 0 until mAllocations.size) {
                mAllocations.removeAt(index)
            }
        }
    }

    fun removeElement(element: T){
        synchronized(mAllocations){
            mAllocations.remove(element)
        }
    }

    fun clearAll() {
        synchronized(mAllocations){
            mAllocations.clear()
        }
    }
}