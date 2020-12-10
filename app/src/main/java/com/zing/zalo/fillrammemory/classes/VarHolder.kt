package com.zing.zalo.fillrammemory.classes

class VarHolder<T> {
    var mAllocations = ArrayList<T>()


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

    fun removeElement(element: T?){
        synchronized(mAllocations){
            if (element != null)
                mAllocations.remove(element)
        }
    }

    fun getAt(index: Int): T? {
        return if (index >= mAllocations.size) null
        else mAllocations[index]
    }

    fun removeLastElement():T?{
        return if(mAllocations.isEmpty()) null
        else mAllocations.removeAt(mAllocations.size - 1)
    }

    fun getLastElement(): T? {
        return if(mAllocations.isEmpty()) null
        else mAllocations[mAllocations.size - 1]
    }

    fun clearAll() {
        synchronized(mAllocations){
            mAllocations.clear()
        }
    }

    fun getSumSize(): Long{
        var size: Long = 0
        synchronized(mAllocations){
            for (i in 0 until mAllocations.size){
                size += (mAllocations[i] as ByteArray).size
            }
            return size
        }
    }

    fun getLength(): Int{
        return mAllocations.size
    }
}