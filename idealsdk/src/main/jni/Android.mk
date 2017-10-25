LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := main
LOCAL_SRC_FILES = unity/libmain.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := mono
LOCAL_SRC_FILES = unity/libmono.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := unity
LOCAL_SRC_FILES = unity/libunity.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := AugmentedReality
LOCAL_SRC_FILES = unity/libAugmentedReality.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
OPENCV_LIB_TYPE:=STATIC
include D:/android/OpenCV/OpenCV-3.2.0-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := ISARNatvieModel
LOCAL_SRC_FILES := nsgif/ISARLibnsgif.c \
                nsgif/ISARGIF_decoder.c \
                ISARTrack_Native.cpp \
                mediaplayer/ISARVideoPlayerHelper.cpp \
                mediaplayer/ISARSampleUtils.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)/ISARLibnsgif
LOCAL_C_INCLUDES += $(LOCAL_PATH)/ISARGlobal.h
LOCAL_C_INCLUDES += $(LOCAL_PATH)/mediaplayer
LOCAL_C_INCLUDES += $(LOCAL_PATH)/AugmentedReality2DHandle.h

LOCAL_LDLIBS += -llog \
                -landroid \
                -lEGL \
                -lGLESv2 \
                -ljnigraphics

LOCAL_LDFLAGS += -Wl,--fix-cortex-a8

LOCAL_STATIC_LIBRARIES += $(OPENCV_LOCAL_LIBRARIES)
LOCAL_SHARED_LIBRARIES += AugmentedReality

include $(BUILD_SHARED_LIBRARY)
