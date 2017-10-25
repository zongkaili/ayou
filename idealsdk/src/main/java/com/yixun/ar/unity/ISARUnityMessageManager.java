package com.yixun.ar.unity;

import com.unity3d.player.UnityPlayer;

public class ISARUnityMessageManager {
    private static final String UNITY_ISAR_CAMERA = "ISARCamera";
    private static final String UNIT_THEME_CLONE = "ARTheme(Clone)";
    private static final String UNITY_PLANE = "Plane";
    private static final String UNITY_RED_PACKET_CLONE = "RedPacketWidget(Clone)";

    // ---------------------3D model------------------------
    public static void triggerAction(String actionJson) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "TriggerAction", actionJson);
    }

    public static void jumpToPage(String actionJson) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "JumpToPage", actionJson);
    }

    public static void changeModelTexture(String actionJson) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "PlayModelActionWithTextureChange", actionJson);
    }

    public static void playModelAnimation(String actionJson) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "PlayModelActionWithAnimation", actionJson);
    }

    public static void playModelByFloatButton(String id) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "FloatButtonsPress", id);
    }

    public static void changePage(int pageType) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "PlayPage", String.valueOf(pageType));
    }

    /**
     * Call unit,y send animation to unity.
     */
    public static void loadThemeData(String animation) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "LoadThemeData", animation);
    }

    //------------------------------- Camera ---------------------------------------

    public static void initISARData(String data) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "InitISARData", data);
    }

    public static void setResourcesServerUrl(String urlArray) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SetResourcesServerURL", urlArray);
    }

    public static void setSaveDataPath(String cachePath) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SetSaveDataPath", cachePath);
    }

    /**
     * Call unity to start search.
     */
    public static void startSearch() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StartSearch", "");
    }

    public static void setScreenSleep(boolean isSleep) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "ScreenSleep", String.valueOf(isSleep));
    }

    public static void setCameraFocus() {
        ISARCamera.getInstance().arcameraSetFocusMode(0);
    }

    public static void setAddExternalSearchDir(String path) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SetAddExternalSearchDir", path);
    }

    public static void setISARCameraQuality() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SetISARCameraQuality", "");
    }

    /**
     * Call unity to init ar by search from camera.
     *
     * @param tmp
     */
    public static void startThemeFromSearch(String tmp) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StartThemeFromSearch", tmp);
    }

    /**
     * Call unit,y set the path of theme resource folder to unity.
     */
    public static void setThemeResourceFolder(String path) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "SetThemeResourceFolder", path);
    }

    /**
     * Call unity load template.
     *
     * @param name
     */
    public static void loadTemplate(String name) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "LoadTemplate", name);
    }

    /**
     * Call unity start ar.
     */
    public static void startAR() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StartAR", "");
    }

    public static void stopAR() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StopAR", "");
    }

    public static void setISARThemeStatus(int type) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SetISARThemeStatus", String.valueOf(type));
    }

    public static void setCameraRotate(String rotateVector) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SetCameraRotate", rotateVector);
    }

    public static void setThemeImageHide(String hideString) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SetThemeImageHidden", hideString);
    }

    public static void startThemeFromOther(String tmp) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StartThemeFromOther", tmp);
    }

    /**
     * Call unity to stop ar theme.
     */
    public static void stopARTheme(int destroyType) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StopSearch", "");
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "DestroyARTheme", String.valueOf(destroyType));
    }

    public static void startScreenRecording() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StartScreenRecording", "");
    }

    public static void stopScreenRecording() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "StopScreenRecording", "");
    }

    public static void makeScreenCapture() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "MakeScreenCapture", "");
    }

    public static void openFlashTorch() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "OpenFlashTorch", "");
    }

    public static void closeFlashTorch() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "CloseFlashTorch", "");
    }

    public static void switchCamera(boolean backCamera) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "SwitchCamera", Boolean.toString(backCamera));
    }

    public static void enableScanEffect(boolean isOpen) {
        UnityPlayer.UnitySendMessage(UNITY_PLANE, "EnableScanEffect", Boolean.toString(isOpen));
    }

    //-------------------------- ARObject ---------------------------
    public static void setARObjectSupportMove(boolean isSupport) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "ARThemeIsSupportMove", String.valueOf(isSupport));
    }

    public static void setARObjectSupportScale(boolean isSupport) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "ARThemeIsSupportScale", String.valueOf(isSupport));
    }

    public static void setARObjectSupportRotate(boolean isSupport) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "ARThemeIsSupportRotate", String.valueOf(isSupport));
    }

    public static void resetARObjectPosition() {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "ResetARThemePosition", "");
    }

    public static void setARObjectSupportFreeMode(boolean isSupport) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "ARThemeIsSupportFreeMode", String.valueOf(isSupport));
    }

    //-------------RedPacket--------------
    public static void startGame(String filePath) {
        UnityPlayer.UnitySendMessage(UNITY_RED_PACKET_CLONE, "LoadGame", "file://" + filePath);
    }

    public static void pauseGame(boolean isPause) {
        UnityPlayer.UnitySendMessage(UNITY_RED_PACKET_CLONE, "PauseRedPacket", Boolean.toString(isPause));
    }

    public static void stopGame() {
        UnityPlayer.UnitySendMessage(UNITY_RED_PACKET_CLONE, "DestroyGame", "");
    }

    public static void setAllowMovePage(boolean isAllow) {
        UnityPlayer.UnitySendMessage(UNIT_THEME_CLONE, "SetMovePageState", Boolean.toString(isAllow));
    }

    public static void prepareGameData(String jsonData) {
        UnityPlayer.UnitySendMessage(UNITY_RED_PACKET_CLONE, "PrepareGameData", jsonData);
    }

    public static void hideTheme(boolean isHide) {
        UnityPlayer.UnitySendMessage(UNITY_ISAR_CAMERA, "ThemeHidden", Boolean.toString(isHide));
    }
}
