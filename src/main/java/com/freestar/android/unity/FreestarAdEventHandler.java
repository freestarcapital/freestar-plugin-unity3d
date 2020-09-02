package com.freestar.android.unity;

import com.freestar.android.ads.ChocolateLogger;
import com.freestar.android.ads.InitCallback;
import com.freestar.android.ads.InterstitialAdListener;
import com.freestar.android.ads.RewardedAdListener;

class FreestarAdEventHandler implements InterstitialAdListener, RewardedAdListener, InitCallback {

    private static final String TAG = "FreestarUnityPlugin";
    static final String INTERSTITIAL_AD_TYPE = "FULLSCREEN_INTERSTITIAL";
    static final String REWARDED_AD_TYPE = "FULLSCREEN_REWARDED";

    private static final String FREESTAR_SUCCESSFULLY_INITIALIZED = "FREESTAR_SUCCESSFULLY_INITIALIZED";
    private static final String FREESTAR_FAILED_TO_INITIALIZE = "FREESTAR_FAILED_TO_INITIALIZE";

    static final String INTERSTITIAL_AD_LOADED = "INTERSTITIAL_AD_LOADED";
    private static final String INTERSTITIAL_AD_FAILED = "INTERSTITIAL_AD_FAILED";
    private static final String INTERSTITIAL_AD_SHOWN = "INTERSTITIAL_AD_SHOWN";
    private static final String INTERSTITIAL_AD_DISMISSED = "INTERSTITIAL_AD_DISMISSED";
    private static final String INTERSTITIAL_AD_CLICKED = "INTERSTITIAL_AD_CLICKED";

    static final String REWARDED_AD_LOADED = "REWARDED_AD_LOADED";
    private static final String REWARDED_AD_FAILED = "REWARDED_AD_FAILED";
    private static final String REWARDED_AD_SHOWN = "REWARDED_AD_SHOWN";
    private static final String REWARDED_AD_SHOWN_ERROR = "REWARDED_AD_SHOWN_ERROR";
    private static final String REWARDED_AD_DISMISSED = "REWARDED_AD_DISMISSED";
    private static final String REWARDED_AD_COMPLETED = "REWARDED_AD_COMPLETED";

    private String mAdType;
    private FreestarAdUnityListener mListener;

    FreestarAdEventHandler(String adType, FreestarAdUnityListener listener) {
        this.mAdType = adType;
        this.mListener = listener;
    }

    private void sendMessageToUnity(String placement, String message) {
        try {
            if (this.mListener != null) {
                ChocolateLogger.d(TAG, "Freestar Ad Event To Java : " + message + " : For Ad Type : " + mAdType);
                this.mListener.onFreestarAdEvent(placement, mAdType, message);
            } else {
                ChocolateLogger.d(TAG, "Unity Listener Null");
            }
        }catch (Exception e) {
            ChocolateLogger.e(TAG,"sendMessageToUnity",e);
        }
    }

    ////////////////// Interstitial Ad Callback //////////////////
    @Override
    public void onInterstitialLoaded(String placement) {
        FreestarPlugin.resetRequest();
        ChocolateLogger.d(TAG, "onInterstitialLoaded");
        sendMessageToUnity(placement, INTERSTITIAL_AD_LOADED);
    }

    @Override
    public void onInterstitialFailed(String placement, int errorCode) {
        FreestarPlugin.resetRequest();
        sendMessageToUnity(placement, INTERSTITIAL_AD_FAILED);
        ChocolateLogger.d(TAG, "onInterstitialFailed");
    }

    @Override
    public void onInterstitialShown(String placement) {
        sendMessageToUnity(placement, INTERSTITIAL_AD_SHOWN);
        ChocolateLogger.d(TAG, "onInterstitialShown");
    }

    @Override
    public void onInterstitialClicked(String placement) {
        sendMessageToUnity(placement, INTERSTITIAL_AD_CLICKED);
        ChocolateLogger.d(TAG, "onInterstitialClicked");
    }

    @Override
    public void onInterstitialDismissed(String placement) {
        FreestarPlugin.resetRequest();
        sendMessageToUnity(placement, INTERSTITIAL_AD_DISMISSED);
        ChocolateLogger.d(TAG, "onInterstitialDismissed");
    }

    ////////////////// Reward Ad Callback //////////////////
    @Override
    public void onRewardedVideoLoaded(String placement) {
        FreestarPlugin.resetRequest();
        sendMessageToUnity(placement, REWARDED_AD_LOADED);
        ChocolateLogger.d(TAG, "onRewardedVideoLoaded");
    }

    @Override
    public void onRewardedVideoFailed(String placement, int errorCode) {
        FreestarPlugin.resetRequest();
        sendMessageToUnity(placement, REWARDED_AD_FAILED);
        ChocolateLogger.d(TAG, "onRewardedVideoFailed");
    }

    @Override
    public void onRewardedVideoShown(String placement) {
        sendMessageToUnity(placement, REWARDED_AD_SHOWN);
        ChocolateLogger.d(TAG, "onRewardedVideoShown");
    }

    @Override
    public void onRewardedVideoShownError(String placement, int errorCode) {
        FreestarPlugin.resetRequest();
        sendMessageToUnity(placement, REWARDED_AD_SHOWN_ERROR);
        ChocolateLogger.d(TAG, "onRewardedVideoShownError");
    }

    @Override
    public void onRewardedVideoDismissed(String placement) {
        FreestarPlugin.resetRequest();
        sendMessageToUnity(placement, REWARDED_AD_DISMISSED);
    }

    @Override
    public void onRewardedVideoCompleted(String placement) {
        FreestarPlugin.resetRequest();
        sendMessageToUnity(placement, REWARDED_AD_COMPLETED);
        ChocolateLogger.d(TAG, "onRewardedVideoCompleted");
    }

    @Override
    public void onError(String message) {
        sendMessageToUnity("", FREESTAR_FAILED_TO_INITIALIZE);
        ChocolateLogger.d(TAG, "initialization failure");
    }

    @Override
    public void onSuccess() {
        sendMessageToUnity("", FREESTAR_SUCCESSFULLY_INITIALIZED);
        ChocolateLogger.d(TAG, "initialized");
    }
}
