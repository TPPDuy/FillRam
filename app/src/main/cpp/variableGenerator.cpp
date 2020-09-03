//
// Created by Darren Tran on 8/25/20.
//
#include <cstring>
#include <jni.h>
#include <malloc.h>
#include <android/log.h>
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_fillrammemory_Services_MemoryForegroundService_varGenerator(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong size) {
    auto* buffer = (char*) malloc(size);
    if (buffer != nullptr) {
        memset(buffer, 1 , size);
    }
    jobject directBuffer = (env)->NewDirectByteBuffer(buffer, size);
    return directBuffer;
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_fillrammemory_Services_MemoryForegroundService_freeVar(JNIEnv *env, jobject thiz,
                                                                        jobject buff) {
    char* buffer;
    jobject directBuff;
    buffer = (char*)(env)->GetDirectBufferAddress(directBuff);
    free(buffer);
}