/*
 ============================================================================
 Name        : gif_decoder.c
 Author      : huzongyao
 Version     :
 Copyright   : Your copyright notice
 Description : these code is called by java and used to decode gif file
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include "ISARLibnsgif.h"
#include "ISARGlobal.h"

#define FUNC(f) Java_com_idealsee_sdk_util_ISARGIFDecoder_##f

void *bitmap_create(int width, int height) {
	return calloc(width * height, 4);
}

void bitmap_set_opaque(void *bitmap, bool opaque) {
	(void) opaque; /* unused */
}

bool bitmap_test_opaque(void *bitmap) {
	return false;
}

unsigned char *bitmap_get_buffer(void *bitmap) {
	return bitmap;
}

void bitmap_destroy(void *bitmap) {
	free(bitmap);
}

void bitmap_modified(void *bitmap) {
	return;
}

gif_bitmap_callback_vt bitmap_callbacks = { bitmap_create, bitmap_destroy, bitmap_get_buffer, bitmap_set_opaque, bitmap_test_opaque, bitmap_modified };

void showError(const char *context, gif_result code) {
	switch (code) {
	case GIF_INSUFFICIENT_FRAME_DATA:
		LOGE("%s failed: GIF_INSUFFICIENT_FRAME_DATA", context);
		break;
	case GIF_FRAME_DATA_ERROR:
		LOGE("%s failed: GIF_FRAME_DATA_ERROR", context);
		break;
	case GIF_INSUFFICIENT_DATA:
		LOGE("%s failed: GIF_INSUFFICIENT_DATA", context);
		break;
	case GIF_DATA_ERROR:
		LOGE("%s failed: GIF_DATA_ERROR", context);
		break;
	case GIF_INSUFFICIENT_MEMORY:
		LOGE("%s failed: GIF_INSUFFICIENT_MEMORY", context);
		break;
	default:
		LOGE("%s failed: unknown code %i", context, code);
		break;
	}
}

int getFrame(gif_animation* gif, int index) {
	gif_result code;
	if (gif == NULL) {
		LOGE("gif is null");
		return -1;
	}
	if (index < 0 || index >= gif->frame_count) {
		LOGE("index error");
		return -1;
	}
	code = gif_decode_frame(gif, index);
	if (code != GIF_OK) {
		showError("gif_decode_frame", code);
		return -1;
	}
	return gif->frames[index].frame_delay;
}

/**
 * load gif file to buffer
 */
unsigned char* loadFile(const char *path, size_t* pFileLength) {
	FILE *fd;
	struct stat sb;
	long long size;
	unsigned char* data = NULL;
	size_t n;

	fd = fopen(path, "rb");
	if (!fd) {
		LOGE("File open error[%s]", path);
		return NULL;
	}
	if (stat(path, &sb)) {
		fclose(fd);
		LOGE("File size error[%s]", path);
		return NULL;
	}
	size = sb.st_size;
	data = malloc(size);
	if (data == NULL) {
		LOGE("Unable to allocate [%lld] bytes", (long long) size);
		fclose(fd);
		return NULL;
	}
	n = fread(data, 1, size, fd);
	fclose(fd);
	if (n != size) {
		LOGE("Read to buffer from [%s] error", path);
		free(data);
		return NULL;
	}
	*pFileLength = size;
	return data;
}

/**
 * init the decoder and scan get the gif information
 */
int initDecoder(const char* filePath, unsigned char* buffer, size_t bufferLength, int* params) {
	gif_result code;

	//malloc gif space
	gif_animation* gif = (gif_animation*) malloc(sizeof(gif_animation));
	if (gif == NULL) {
		LOGE("Unable to allocate gif_animation");
		return -1;
	}
	/* create our gif animation */
	gif_create(gif, &bitmap_callbacks);
	if(buffer == NULL){
		/* load file into memory */
		buffer = loadFile(filePath, &bufferLength);
	}
	if (buffer == NULL) {
		free(gif);
		LOGE("gif_data read failed");
		return -1;
	}
	/* begin decoding */
	do {
		code = gif_initialise(gif, bufferLength, buffer);
		if (code != GIF_OK && code != GIF_WORKING) {
			showError("gif_initialise", code);
			return -1;
		}
	} while (code != GIF_OK);
	params[0] = gif->frame_count;
	params[1] = gif->width;
	params[2] = gif->height;
	params[3] = gif->loop_count;
	params[4] = (int) gif;
	return 0;
}

JNIEXPORT jint JNICALL FUNC(nInitByPath)(JNIEnv *env, jobject thiz, jstring filePath, jintArray params) {
	int ret = 0;
	char* cfilePath = (char*) (*env)->GetStringUTFChars(env, filePath, NULL);
	int* cParams = (*env)->GetIntArrayElements(env, params, NULL);
	ret = initDecoder(cfilePath, NULL, 0, cParams);
	(*env)->ReleaseIntArrayElements(env, params, cParams, 0);
	(*env)->ReleaseStringUTFChars(env, filePath, cfilePath);
	return ret;
}

JNIEXPORT jint JNICALL FUNC(nInitByBytes)(JNIEnv *env, jobject thiz, jbyteArray buffer, jintArray params) {
	int ret = 0;
	int bufferLength = (*env)->GetArrayLength(env, buffer);
	int* cParams = (*env)->GetIntArrayElements(env, params, NULL);
	unsigned char* cBuffer = (unsigned char*)(*env)->GetByteArrayElements(env, buffer, NULL);
	unsigned char* cBufferCopy = (unsigned char*)malloc(bufferLength);
	memcpy(cBufferCopy, cBuffer, bufferLength);
	ret = initDecoder(NULL, cBufferCopy, bufferLength, cParams);
	(*env)->ReleaseIntArrayElements(env, params, cParams, 0);
	return ret;
}

JNIEXPORT jint JNICALL FUNC(nGetFrameBitmap)(JNIEnv *env, jobject thiz, jint index, jobject jbitmap, jint handler) {
	int ret = 0;
	int delay = 0;
	void* bmpAddr = NULL;
	gif_animation* gif = NULL;
	int buf_size = 0;

	gif = (gif_animation*) handler;
	if (gif == NULL) {
		LOGE("gif is null");
		return -1;
	}
	buf_size = gif->width * gif->height * 4;
	delay = getFrame(gif, index);
//	if (delay <= 0) {
//		return -1;
//	}
	if ((ret = AndroidBitmap_lockPixels(env, jbitmap, &bmpAddr)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return ret;
	}
	memcpy(bmpAddr, gif->frame_image, buf_size);
	AndroidBitmap_unlockPixels(env, jbitmap);
	return delay * 10;
}

JNIEXPORT jint JNICALL FUNC(nDestory)(JNIEnv *env, jobject thiz, jint handler) {
	gif_animation* gif = (gif_animation*) handler;
	if (gif == NULL) {
		LOGE("gif is null");
		return -1;
	} else {
		free(gif->gif_data);
		gif_finalise(gif);
		free(gif);
	}
	return 0;
}

