//
// Created by Darren Tran on 8/25/20.
//
#include <cstring>
#include <jni.h>
#include <malloc.h>
#include <android/log.h>
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_fillrammemory_services_MemoryService_varGenerator(JNIEnv *env,
                           jobject instance,
                           jlong size) {
    auto* buffer = (char*) malloc(size);
    if (buffer != nullptr) {
        memset(buffer, 1 , size);
    }
    jobject directBuffer = (env)->NewDirectByteBuffer(buffer, size);
    return directBuffer;
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_fillrammemory_services_MemoryService_freeVar(JNIEnv *env, jobject instance, jobject buff) {
    jobject directBuff = nullptr;
    directBuff = reinterpret_cast<jobject>((char *) (env)->GetDirectBufferAddress(buff));
    free(directBuff);
}