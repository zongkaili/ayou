#include "ISARGlobal.h"
#include "AugmentedReality2DHandle.h"

#define FUNCTRACK(f) Java_com_idealsee_sdk_util_ISARNativeTrackUtil_##f
#ifdef __cplusplus
extern "C" {
#endif
static char *yBuffer = NULL;
static char *pYuvSrc = NULL;
static int cameraWidth, cameraHeight, yLength, yuvLength;
static int saveCount = 0;
static std::vector<std::string> datFiles;

JNIEXPORT void JNICALL FUNCTRACK(initNative)(JNIEnv *env, jobject obj, jint width, jint height) {
    LOGD("jni initNative");
    cameraWidth = width;
    cameraHeight = height;
    yLength = cameraWidth * cameraHeight;
    yuvLength = yLength * 3 / 2;
    if (yBuffer != NULL) {
        free(yBuffer);
        yBuffer = NULL;
    }
    if (pYuvSrc != NULL) {
        free(pYuvSrc);
        pYuvSrc = NULL;
    }
    yBuffer = (char *) malloc(yLength * sizeof(char));
    pYuvSrc = (char *) malloc(yuvLength * sizeof(char));
}

JNIEXPORT void JNICALL FUNCTRACK(deInitNative)(JNIEnv *env, jobject obj) {
    LOGD("jni deinitNative");
    if (yBuffer != NULL) {
        free(yBuffer);
        yBuffer = NULL;
    }
    if (pYuvSrc != NULL) {
        free(pYuvSrc);
        pYuvSrc = NULL;
    }
}

JNIEXPORT void JNICALL FUNCTRACK(nAugmentedInit)(JNIEnv *env, jobject obj, jfloat fx, jfloat fy,
                                                 jfloat cx,
                                                 jfloat cy) {
    LOGD("jni AugmentedReality2D_init");
    AugmentedReality2D_init(fx, fy, cx, cy);
}

JNIEXPORT jint JNICALL FUNCTRACK(nAugmentedRun)(JNIEnv *env, jobject obj, jbyteArray byteArray) {
    // LOGD("jni AugmentedReality2D_run");
    size_t length = env->GetArrayLength(byteArray);
    if (0 == length) {
        LOGW("jni AugmentedReality2D_run length is 0.");
        return 1;
    }
    jboolean isCopy;
    jbyte *data = env->GetByteArrayElements(byteArray, &isCopy);
    // data length is more than yLength, because java ByteBuffer size
//    memset(yBuffer, 0, yLength);
//    memcpy(yBuffer, data, yLength);

//    IplImage *yFrame = cvCreateImageHeader(cvSize(cameraWidth, cameraHeight), IPL_DEPTH_8U, 1);
//    cvSetData(yFrame, data, yFrame->widthStep);
    //cv::Mat grayImage(yFrame);
    /*char commond[50];
    snprintf(commond, sizeof(commond), "/sdcard/11/gray%03d.jpg", saveCount);
    saveCount++;
    LOGD("jni AugmentedReality2D_run command=[%s]", commond);
    cv::Mat grayImage = cv::imread(commond, 0);
    LOGD("jni AugmentedReality2D_run command end");*/
//    cv::Mat grayImage = cv::cvarrToMat(yFrame);
    //cv::Mat grey;
    //cv::resize(grayImage, grey, cv::Size(grayImage.cols / 2, grayImage.rows / 2), 0, 0, cv::INTER_AREA);
    // for test to save file
    /*LOGD("jni AugmentedReality2D_run zhang save");
    char commond[50];
        snprintf(commond, sizeof(commond), "/sdcard/11/gray%03d.jpg", saveCount);
        std::vector<int> compression_params;
        compression_params.push_back(CV_IMWRITE_JPEG_QUALITY);
        compression_params.push_back(50);
        cv::imwrite(commond, grayImage, compression_params);
        saveCount++;*/
//    if (saveCount < 50) {
//    }
    //LOGD("cameraHeight : %d / %d",cameraHeight,cameraWidth);
    cv::Mat grayImage(cameraHeight, cameraWidth, CV_8UC1);
    //LOGD("jni AugmentedReality2D_run command=[%d]", length);
    memcpy(grayImage.data, data, length);
    //LOGW("memcpy.");
    int status = AugmentedReality2D_run(grayImage);
//    grey.release();
    grayImage.release();
//    cvReleaseImageHeader(&yFrame);
//    free(data);
    env->ReleaseByteArrayElements(byteArray, data, 0);
    // LOGD("jni AugmentedReality2D_run end");
    return status;
}

JNIEXPORT void JNICALL FUNCTRACK(nAugmentedGetPoseForUnity)(JNIEnv *env, jobject obj, jint status) {
    std::vector<float> pose = AugmentedReality2D_getPoseForUnity(1);
//    LOGI("nAugmentedGetPoseForUnity-------.");
    jclass clazz_camera = env->FindClass("com/idealsee/ar/unity/ISARCamera");
    //获取father对象中的function方法的id
    jmethodID id_updatePosition = env->GetStaticMethodID(clazz_camera, "updatePosition", "(FFF)V");
    jmethodID id_updateOrientation = env->GetStaticMethodID(clazz_camera, "updateOrientation",
                                                            "(FFFF)V");
//    LOGD("cameraClass id_updatePosition=%d, id_updateOrientation=%d", id_updatePosition, id_updateOrientation);

    if (id_updatePosition == 0) {
        LOGI("Function updatePosition() not found.");
    } else {
//        LOGI("call updatePosition.");
        env->CallStaticVoidMethod(clazz_camera, id_updatePosition, pose[0], pose[1], pose[2]);
//        LOGI("call updatePosition end.");
    }
    if (id_updateOrientation == 0) {
        LOGI("Function updateOrientation() not found.");
    } else {
        env->CallStaticVoidMethod(clazz_camera, id_updateOrientation, pose[3], pose[4], pose[5],
                                  pose[6]);
    }
    env->DeleteLocalRef(clazz_camera);
}

JNIEXPORT int JNICALL FUNCTRACK(nAugmentedLoadFile)(JNIEnv *env, jobject obj, jstring filepath) {
    const char *csrcpath = env->GetStringUTFChars(filepath, 0);
    std::string pathStr = csrcpath;
    int status = AugmentedReality2D_loadFile(pathStr);

    env->ReleaseStringUTFChars(filepath, csrcpath);
    return status;
}

JNIEXPORT jint JNICALL FUNCTRACK(saveYPicture)(JNIEnv *env, jobject obj, jbyteArray byteArray,
                                               jstring dstPath) {
//    LOGD("jni AugmentedReality2D_run");
    const char *cdstPath = env->GetStringUTFChars(dstPath, 0);
    size_t length = env->GetArrayLength(byteArray);
    if (0 == length) {
        LOGW("jni saveYPicture length is 0.");
        return 1;
    }
    jboolean isCopy;
    jbyte *data = env->GetByteArrayElements(byteArray, &isCopy);
    // data length is more than yLength, because java ByteBuffer size
//    memcpy(yBuffer, data, yLength);
//    IplImage *yFrame = cvCreateImageHeader(cvSize(cameraWidth, cameraHeight), IPL_DEPTH_8U, 1);
//    cvSetData(yFrame, yBuffer, yFrame->widthStep);
    //cv::Mat grayImage(yFrame);
//    cv::Mat grayImage = cv::cvarrToMat(yFrame);
    std::vector<int> compression_params;
    compression_params.push_back(CV_IMWRITE_JPEG_QUALITY);
    //	compression_params.push_back(95);
    compression_params.push_back(50);

    cv::Mat grayImage(cameraHeight, cameraWidth, CV_8UC1);
    memcpy(grayImage.data, data, length);

    cv::imwrite(cdstPath, grayImage, compression_params);
    grayImage.release();

    env->ReleaseByteArrayElements(byteArray, data, 0);
    env->ReleaseStringUTFChars(dstPath, cdstPath);
    return 0;
}

JNIEXPORT jint JNICALL FUNCTRACK(localRecognition)(JNIEnv *env, jobject obj, jbyteArray byteArray,
                                                   jobjectArray objArray) {
    size_t objLength = env->GetArrayLength(objArray);
    if (0 == objLength) {
        LOGW("jni AugmentedReality2D_run length is 0.");
        return 1;
    }
    jboolean isCopy;
    jbyte *data = env->GetByteArrayElements(byteArray, &isCopy);
    // data length is more than yLength, because java ByteBuffer size
    IplImage *yFrame = cvCreateImageHeader(cvSize(cameraWidth, cameraHeight), IPL_DEPTH_8U, 1);
    cvSetData(yFrame, data, yFrame->widthStep);
    //cv::Mat grayImage(yFrame);
    cv::Mat grayImage = cv::cvarrToMat(yFrame);
    std::vector<std::string> tempFiles;
    for (int i = 0; i < objLength; i++) {
        jstring obja = (jstring) env->GetObjectArrayElement(objArray, i);
        const char *chars = env->GetStringUTFChars(obja, NULL);
        tempFiles.push_back(chars);
        env->ReleaseStringUTFChars(obja, chars);
    }
    /*for (int i=0; i< objLength; i++) {
        LOGD("localRecognition i=%d,%s", i, tempFiles.at(i).c_str());
    }*/
    int status = AugmentedReality2D_localRecognition(grayImage, tempFiles);
    tempFiles.clear();
    grayImage.release();
    cvReleaseImageHeader(&yFrame);
    env->ReleaseByteArrayElements(byteArray, data, 0);
    return status;
}

JNIEXPORT jstring JNICALL FUNCTRACK(localRecognition2)(JNIEnv *env, jobject obj, jbyteArray byteArray) {
    LOGW("jni AugmentedReality2D_run localRecognition2.");
    size_t objLength = datFiles.size();
    size_t byteLength = env->GetArrayLength(byteArray);
    jstring str = NULL;
    if (0 == objLength || 0 == byteLength) {
        LOGW("jni AugmentedReality2D_run length is 0.");
        return str;
    }
    jboolean isCopy;
    jbyte *data = env->GetByteArrayElements(byteArray, &isCopy);
    // data length is more than yLength, because java ByteBuffer size
    /*IplImage *yFrame = cvCreateImageHeader(cvSize(cameraWidth, cameraHeight), IPL_DEPTH_8U, 1);
    cvSetData(yFrame, data, yFrame->widthStep);
//    cv::Mat grayImage(yFrame);
    cv::Mat grayImage = cv::cvarrToMat(yFrame);*/

    /*for (int i = 0; i < objLength; i++) {
        LOGD("localRecognition i=%d,%s", i, datFiles.at(i).c_str());
    }*/

    cv::Mat grayImage(cameraHeight, cameraWidth, CV_8UC1);
    memcpy(grayImage.data, data, byteLength);

    /* char* grayPath = "/sdcard/22/gray.jpg";
     LOGD("localRecognition graPath[%s]", grayPath);
     cv::Mat testImage = cv::imread(grayPath, CV_LOAD_IMAGE_GRAYSCALE);*/
    for (int i = 0; i < datFiles.size(); i++) {
        std::string tempDat = datFiles.at(i);
        std::vector<std::string> tempArray;
        tempArray.push_back(tempDat);
        int temStatus = AugmentedReality2D_localRecognition(grayImage, tempArray);
        tempArray.clear();
        std::vector<std::string>().swap(tempArray);
        if (temStatus >= 0) {
            str = env->NewStringUTF(tempDat.c_str());
            LOGW("jni AugmentedReality2D_run temStatus : " + temStatus + " str : " + str);
            break;
        }
    }
    //int status = AugmentedReality2D_localRecognition(grayImage, datFiles);

//    testImage.release();
    env->ReleaseByteArrayElements(byteArray, data, 0);
    //grayImage.release();

/*    std::vector<int> compression_params;
compression_params.push_back(CV_IMWRITE_JPEG_QUALITY);
//	compression_params.push_back(95);
compression_params.push_back(100);
cv::imwrite("/sdcard/local.jpg", grayImage, compression_params);*/
    grayImage.release();
    return str;
}
JNIEXPORT jintArray JNICALL FUNCTRACK(getScreenSize)(JNIEnv *env, jobject obj) {
    cv::Size size = AugmentedReality2D_getTempSize();
    jintArray array = env->NewIntArray(2);
    jint *arr = env->GetIntArrayElements(array, NULL);
    arr[0] = size.width;
    arr[1] = size.height;
    LOGD("arr = %d,%d,%d,%d", size.width, size.height, arr[0], arr[1]);
    env->ReleaseIntArrayElements(array, arr, 0);
    return array;
}

JNIEXPORT void JNICALL FUNCTRACK(setDatPath) (JNIEnv *env, jobject obj, jobjectArray objArray) {
    size_t objLength = env->GetArrayLength(objArray);
    if (0 == objLength) {
        LOGW("jni AugmentedReality2D_run length is 0.");
        return;
    }
    datFiles.clear();
    for (int i = 0; i < objLength; i++) {
        jstring obja = (jstring) env->GetObjectArrayElement(objArray, i);
        const char *chars = env->GetStringUTFChars(obja, NULL);
        datFiles.push_back(chars);
        env->ReleaseStringUTFChars(obja, chars);
    }
    /*for (int i=0; i< objLength; i++) {
        LOGD("localRecognition i=%d,%s", i, datFiles.at(i).c_str());
    }*/
}

JNIEXPORT void JNICALL FUNCTRACK(clearDatPath) (JNIEnv *env, jobject obj) {
    datFiles.clear();
}

#ifdef __cplusplus
}
#endif
