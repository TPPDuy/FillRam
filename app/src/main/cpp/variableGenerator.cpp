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
Java_com_zing_zalo_fillrammemory_services_MemoryService_varGenerator(JNIEnv *env,
                           jobject instance,
                           jlong size) {

    auto* buffer = (jbyte*)malloc(size);
    if(buffer != nullptr){
        memset(buffer, 0, size);
        jobject directBuffer = (env)->NewDirectByteBuffer(buffer, size);
        return directBuffer;
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_zing_zalo_fillrammemory_services_MemoryService_varExtend(JNIEnv *env, jobject instance, jobject buff, jlong newSize) {
    jbyte* byteBuff = static_cast<jbyte*>(env->GetDirectBufferAddress(buff));
    if (byteBuff != nullptr) {
        syslog(LOG_CRIT, "buff is found");
        char *newSpace = (char *) (realloc(byteBuff, newSize));
        if (newSpace != nullptr) {
            syslog(LOG_CRIT, "can find new space");
            memset(newSpace, 0, newSize);
            jobject directBuffer = (env)->NewDirectByteBuffer(newSpace, newSize);
            return directBuffer;
        }
    }
    return nullptr;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_zing_zalo_fillrammemory_services_MemoryService_freeVar(JNIEnv *env, jobject instance, jobject buff) {
    jobject directBuff;
    directBuff = reinterpret_cast<jobject>((jbyte*) (env)->GetDirectBufferAddress(buff));
    free(directBuff);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_zing_zalo_fillrammemory_services_MemoryService_changeAllocatedSize(JNIEnv *env, jobject instance, jobject buff, jlong newSize){
    if(buff!= nullptr) {
        auto *byteBuff = static_cast<jbyte *>(env->GetDirectBufferAddress(buff));
        if (byteBuff != nullptr) {
            free(byteBuff);
            auto *newSpace = (jbyte *) malloc(newSize);
            if (newSpace != nullptr) {
                syslog(LOG_CRIT, "can find new space");
                memset(newSpace, 0, newSize);
                jobject directBuffer = (env)->NewDirectByteBuffer(newSpace, newSize);
                return directBuffer;
            } else {
                //if cannot realloc, return the old buff
                syslog(LOG_CRIT, "cannot find new space");
                return nullptr;
            }
        }
    }
    return nullptr;
}
