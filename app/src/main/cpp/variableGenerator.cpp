//
// Created by Darren Tran on 8/25/20.
//
#include <cstring>
#include <jni.h>
#include <malloc.h>
#include <android/log.h>

extern "C" {
    JNIEXPORT jobject JNICALL Java_com_example_fillrammemory_Controller_MainActivity_varGenerator(JNIEnv *env, jobject instance, jlong size){
        unsigned char* buffer = (unsigned char*) malloc(1024*1024*500);
        if (buffer != NULL) {
            memset(buffer, 1 , 1024*1024*500);
        }
        jobject directBuffer = (env)->NewDirectByteBuffer(buffer, 1024*1024*500);
        return directBuffer;
    }
}