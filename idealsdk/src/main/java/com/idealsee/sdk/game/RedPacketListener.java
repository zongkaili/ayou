package com.idealsee.sdk.game;

public interface RedPacketListener {
    public void prepareGame(String auth, RedPacketInfo redPacketInfo);

    public void onGameStarted();

    public void onGameTimeUpdate(int time);

    public void onGameCatchSomething(int objectType, float amount);

    public void onGamePositionStatus(int status);

    public void onGameMissTarget(int status);

    public void onGameLastThreeTimes(int times);

    public void onGameStopped();

    public void onGameShareSuccess();

    public void onGameShareFailed();
}
