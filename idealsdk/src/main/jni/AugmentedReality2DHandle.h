/* 2016-01-11 13:54:07
 * Author: Zhuo.Chen
 * File: AugmentedReality2DHandle.h
 * Descriptor: AugmentedReality Handle
 * Copyright (C) 2015, IdealSee Inc., all rights reserved.
 * */
/* 2016-01-11 13:54:52 Zhuo.Chen */
#ifndef  AUGMENTEDREALITY2DHANDLE_H_2016_01_11
#define  AUGMENTEDREALITY2DHANDLE_H_2016_01_11
#include <opencv2/opencv.hpp>
#include <string.h>
#define AugmentedReality2D_getTempSize A45D0B1A42AD7021DBCB5980FC5B00DDE
#define AugmentedReality2D_getPoseForUnity A6EFD0034ABF56436BB4695F64D6815A3
#define AugmentedReality2D_run A625376702D54B4C5C37A97F547A95EC1
#define AugmentedReality2D_init  A0358886970764A089277EE0EB5E55B2D
#define AugmentedReality2D_getPose  AF6F30DA533621CA6186932A5FE2F12F7
#define AugmentedReality2D_loadData A4AC3FBFFDC8276270B4995764DDA8AD7
#define AugmentedReality2D_loadFile AD8671DE6491BE6A5A4693406B4CA3FB7
#define AugmentedReality2D_localRecognition A45D0B1A42AD7021DBCB5980FC5BXXXYY
extern "C"
{
__attribute__((visibility("default"))) int AugmentedReality2D_loadFile(const std::string& file);
__attribute__((visibility("default"))) int AugmentedReality2D_loadData(const char* data);
__attribute__((visibility("default"))) int AugmentedReality2D_run(const cv::Mat& imgGray);
__attribute__((visibility("default"))) const std::vector<float>& AugmentedReality2D_getPose();
__attribute__((visibility("default"))) std::vector<float> AugmentedReality2D_getPoseForUnity(const char& flag);
__attribute__((visibility("default"))) void AugmentedReality2D_init(const float& fx, const float& fy, const float& cx, const float& cy);
__attribute__((visibility("default"))) cv::Size AugmentedReality2D_getTempSize();
__attribute__((visibility("default"))) int AugmentedReality2D_localRecognition(const cv::Mat& imgGray, std::vector<std::string>& tempFiles);
}
/* int AugmentedReality2D_getCoords(std::vector<float>& trackedCoords, std::vector<float>& predCoords); */
#endif  /*AUGMENTEDREALITY2DHANDLE_H_2016_01_11*/
