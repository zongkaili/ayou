package com.idealsee.sdk.media.videoplayer;

/**
 * Put YXVideoPlayer into layout
 * From a YXVideoPlayer to another YXVideoPlayer
 */
public class ISARVideoPlayerManager {

    public static ISARVideoPlayer FIRST_FLOOR_JCVD;
    public static ISARVideoPlayer SECOND_FLOOR_JCVD;

    public static void setFirstFloor(ISARVideoPlayer jcVideoPlayer) {
        FIRST_FLOOR_JCVD = jcVideoPlayer;
    }

    public static void setSecondFloor(ISARVideoPlayer jcVideoPlayer) {
        SECOND_FLOOR_JCVD = jcVideoPlayer;
    }

    public static ISARVideoPlayer getFirstFloor() {
        return FIRST_FLOOR_JCVD;
    }

    public static ISARVideoPlayer getSecondFloor() {
        return SECOND_FLOOR_JCVD;
    }

    public static ISARVideoPlayer getCurrentJcvd() {
        if (getSecondFloor() != null) {
            return getSecondFloor();
        }
        return getFirstFloor();
    }

    public static void completeAll() {
        if (SECOND_FLOOR_JCVD != null) {
            SECOND_FLOOR_JCVD.onCompletion();
            SECOND_FLOOR_JCVD = null;
        }
        if (FIRST_FLOOR_JCVD != null) {
            FIRST_FLOOR_JCVD.onCompletion();
            FIRST_FLOOR_JCVD = null;
        }
    }
}
