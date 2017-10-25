#ifndef _ISAR_GLOBAL_H_
#define _ISAR_GLOBAL_H_
#include <android/log.h>
#include <android/bitmap.h>
#include <jni.h>

//#define LOG_NDEBUG 0
#define TAG_DEBUG "IdealNative"
#ifdef LOG_NDEBUG
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG_DEBUG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG_DEBUG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , TAG_DEBUG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , TAG_DEBUG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , TAG_DEBUG, __VA_ARGS__)
#define FUNCTION __FUNCTION__

#else
#define LOGV(...) ((void)0)
#define LOGD(...) ((void)0)
#define LOGI(...) ((void)0)
#define LOGW(...) ((void)0)
#define LOGE(...) ((void)0)
#endif

#endif // _ISAR_GLOBAL_H_
