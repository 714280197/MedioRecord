LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeg
LOCAL_SRC_FILES := libffmpeg.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeginvoke
LOCAL_SRC_FILES := audiorecord_messcat_com_mediorecorddemo_ffmpeg_FFmpegRun.c ffmpeg.c ffmpeg_opt.c cmdutils.c ffmpeg_filter.c
LOCAL_C_INCLUDES := G:\FFmpeg\ffmpeg-3.2.4
LOCAL_LDLIBS := -llog -lz -ldl
LOCAL_SHARED_LIBRARIES := ffmpeg
include $(BUILD_SHARED_LIBRARY)