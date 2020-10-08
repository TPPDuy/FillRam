//
// Created by Darren Tran on 8/25/20.
//
#include <cstring>
#include <jni.h>
#include <malloc.h>
#include <syslog.h>
#include <new>

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_fillrammemory_services_MemoryService_varGenerator(JNIEnv *env,
                           jobject instance,
                           jlong size) {

    auto* buffer = (char*) malloc(size);
    if(buffer!= nullptr){
        memset(buffer, 1 , size);
        for(int i=0; i<size; i++){
            buffer[i] = 0;
        }
        jobject directBuffer = (env)->NewDirectByteBuffer(buffer, size);
        return directBuffer;
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_fillrammemory_services_MemoryService_varExtend(JNIEnv *env, jobject instance, jobject buff, jint oldSize, jint addedSize) {
    char* byteBuff = static_cast<char *>(env->GetDirectBufferAddress(buff));
    if (byteBuff != nullptr){
        syslog(LOG_CRIT, "buff is found");
        int newSize = oldSize + addedSize;
        char* newSpace = (char*)(realloc(byteBuff, newSize));
        if (newSpace != nullptr){
            syslog(LOG_CRIT, "can find new space");
            for(int i = oldSize; i < newSize; i++){
                newSpace[i] = 0;
            }
            jobject directBuffer = (env)->NewDirectByteBuffer(newSpace, newSize);
            return directBuffer;
        } else{
            //if cannot realloc, return the old buff
            syslog(LOG_CRIT, "cannot find new space");
            return (env)->NewDirectByteBuffer(byteBuff, oldSize);
        }
    } else return nullptr;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_fillrammemory_services_MemoryService_freeVar(JNIEnv *env, jobject instance, jobject buff) {
    jobject directBuff = nullptr;
    directBuff = reinterpret_cast<jobject>((char *) (env)->GetDirectBufferAddress(buff));
    free(directBuff);
}