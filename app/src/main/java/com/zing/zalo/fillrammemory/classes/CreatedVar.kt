package com.zing.zalo.fillrammemory.classes

import java.nio.ByteBuffer

data class CreatedVar(var partition: ArrayList<ByteBuffer> = ArrayList(), val createdTime: Long = System.currentTimeMillis()){

    fun getVarSize(): Int{
        var result: Int = 0
        for(variable in partition)
            result += variable.capacity()
        return result
    }

    fun addSubVar(value: ByteBuffer){
        partition.add(value)
    }

    fun replaceAt(value: ByteBuffer, index: Int){
        partition[index] = value
    }

    fun getLastElement(): ByteBuffer{
        return partition[partition.size-1]
    }
}