package com.vdopia.unity.plugin;

import android.util.Log;

import com.freestar.android.ads.RewardedAdListener;
import com.freestar.android.ads.InitCallback;
import com.freestar.android.ads.InterstitialAdListener;

class VdopiaSdkAdEventHandler implements InterstitialAdListener, RewardedAdListener, InitCallback {

    private static final String TAG = "VdopiaUnityPlugin";
    static final String INTERSTITIAL_AD_TYPE = "FULLSCREEN_INTERSTITIAL";
    static final String REWARD_AD_TYPE = "REWARD";
    static final String AD_TYPE_NONE = "";

    private static final String CHOCOLATE_SUCCESSFULLY_INITIALIZED = "CHOCOLATE_SUCCESSFULLY_INITIALIZED";
    private static final String CHOCOLATE_FAILED_TO_INITIALIZE = "CHOCOLATE_FAILED_TO_INITIALIZE";

    private static final String INTERSTITIAL_AD_LOADED = "INTERSTITIAL_LOADED";
    static final String INTERSTITIAL_AD_FAILED = "INTERSTITIAL_FAILED";
    private static final String INTERSTITIAL_AD_SHOWN = "INTERSTITIAL_SHOWN";
    private static final String INTERSTITIAL_AD_DISMISSED = "INTERSTITIAL_DISMISSED";
    private static final String INTERSTITIAL_AD_CLICKED = "INTERSTITIAL_CLICKED";

    private static final String REWARD_AD_LOADED = "REWARD_AD_LOADED";
    static final String REWARD_AD_FAILED = "REWARD_AD_FAILED";
    private static final String REWARD_AD_SHOWN = "REWARD_AD_SHOWN";
    private static final String REWARD_AD_SHOWN_ERROR = "REWARD_AD_SHOWN_ERROR";
    private static final String REWARD_AD_DISMISSED = "REWARD_AD_DISMISSED";
    private static final String REWARD_AD_COMPLETED = "REWARD_AD_COMPLETED";

    private String mAdType;
    private VdopiaAdUnityListener mListener;

    VdopiaSdkAdEventHandler(String adType, VdopiaAdUnityListener listener) {
        this.mAdType = adType;
        this.mListener = listener;
    }

    private void sendMessageToUnity(String message) {
        try {
            if (this.mListener != null) {
                Log.d(TAG, "Vdopia Ad Event To Java : " + message + " : For Ad Type : " + mAdType);
                this.mListener.onVdopiaAdEvent(mAdType, message);
            } else {
                Log.d(TAG, "Unity Listener Null");
            }
        }catch (Exception e) {
            Log.e(TAG,"sendMessageToUnity",e);
        }
    }

    ////////////////// Interstitial Ad Callback //////////////////
    @Override
    public void onInterstitialLoaded(String placement) {
        VdopiaPlugin.resetRequest();
        Log.d(TAG, "onInterstitialLoaded");
        sendMessageToUnity(INTERSTITIAL_AD_LOADED);
    }

    @Override
    public void onInterstitialFailed(String placement, int errorCode) {
        VdopiaPlugin.resetRequest();
        sendMessageToUnity(INTERSTITIAL_AD_FAILED);
        Log.d(TAG, "onInterstitialFailed");
    }

    @Override
    public void onInterstitialShown(String placement) {
        sendMessageToUnity(INTERSTITIAL_AD_SHOWN);
        Log.d(TAG, "onInterstitialShown");
    }

    @Override
    public void onInterstitialClicked(String placement) {
        sendMessageToUnity(INTERSTITIAL_AD_CLICKED);
        Log.d(TAG, "onInterstitialClicked");
    }

    @Override
    public void onInterstitialDismissed(String placement) {
        VdopiaPlugin.resetRequest();
        sendMessageToUnity(INTERSTITIAL_AD_DISMISSED);
        Log.d(TAG, "onInterstitialDismissed");
    }

    ////////////////// Reward Ad Callback //////////////////
    @Override
    public void onRewardedVideoLoaded(String placement) {
        VdopiaPlugin.resetRequest();
        sendMessageToUnity(REWARD_AD_LOADED);
        Log.d(TAG, "onRewardedVideoLoaded");
    }

    @Override
    public void onRewardedVideoFailed(String placement, int errorCode) {
        VdopiaPlugin.resetRequest();
        sendMessageToUnity(REWARD_AD_FAILED);
        Log.d(TAG, "onRewardedVideoFailed");
    }

    @Override
    public void onRewardedVideoShown(String placement) {
        sendMessageToUnity(REWARD_AD_SHOWN);
        Log.d(TAG, "onRewardedVideoShown");
    }

    @Override
    public void onRewardedVideoShownError(String placement, int errorCode) {
        VdopiaPlugin.resetRequest();
        sendMessageToUnity(REWARD_AD_SHOWN_ERROR);
        Log.d(TAG, "onRewardedVideoShownError");
    }

    @Override
    public void onRewardedVideoDismissed(String placement) {
        VdopiaPlugin.resetRequest();
        sendMessageToUnity(REWARD_AD_DISMISSED);
    }

    @Override
    public void onRewardedVideoCompleted(String placement) {
        VdopiaPlugin.resetRequest();
        sendMessageToUnity(REWARD_AD_COMPLETED);
        Log.d(TAG, "onRewardedVideoCompleted");
    }

    @Override
    public void onError(String message) {
        //sendMessageToUnity(CHOCOLATE_FAILED_TO_INITIALIZE);
        Log.d(TAG, "Chocolate initialization failure");
    }

    @Override
    public void onSuccess() {
        sendMessageToUnity(CHOCOLATE_SUCCESSFULLY_INITIALIZED);
        Log.d(TAG, "Chocolate initialized");
    }
}
