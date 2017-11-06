//
// Created by Administrator on 2017/9/19 0019.
//
#include <stdio.h>
#include "audiorecord_messcat_com_mediorecorddemo_ffmpeg_FFmpegRun.h"
#include "ffmpeg.h"
#include "logjam.h"

JNIEXPORT jint JNICALL Java_audiorecord_messcat_com_mediorecorddemo_ffmpeg_FFmpegRun_run
(JNIEnv *env, jclass obj, jobjectArray commands){
    //FFmpeg av_log() callback
    int argc = (*env)->GetArrayLength(env, commands);
    char *argv[argc];

    LOGD("Kit argc %d\n", argc);
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env, commands, i);
        argv[i] = (char*) (*env)->GetStringUTFChars(env, js, 0);
        LOGD("Kit argv %s\n", argv[i]);
    }
    return run(argc, argv);
}