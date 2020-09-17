package com.freestar.android.unity;

import android.view.View;

import com.freestar.android.ads.BannerAdListener;
import com.freestar.android.ads.ChocolateLogger;
import com.freestar.android.ads.ErrorCodes;
import com.freestar.android.ads.InitCallback;
import com.freestar.android.ads.InterstitialAdListener;
import com.freestar.android.ads.RewardedAdListener;

class FreestarAdEventHandler implements InterstitialAdListener, RewardedAdListener,
        BannerAdListener, InitCallback {

    private static final String TAG = "FreestarUnityPlugin";

    private String adType;
    private int adSize;
    private FreestarAdUnityListener freestarAdUnityListener;

    FreestarAdEventHandler(String adType, int adSize, FreestarAdUnityListener freestarAdUnityListener) {
        this.adType = adType;
        this.adSize = adSize;
        this.freestarAdUnityListener = freestarAdUnityListener;
    }

    private void sendMessageToUnity(String placement, String eventMessage) {
        try {
            if (this.freestarAdUnityListener != null) {
                ChocolateLogger.d(TAG, "Freestar Ad Event To Java : " + eventMessage + " : For Ad Type : " + adType);
                this.freestarAdUnityListener.onFreestarAdEvent(placement, adType, adSize, eventMessage);
            } else {
                ChocolateLogger.d(TAG, "Unity Listener Null");
            }
        }catch (Exception e) {
            ChocolateLogger.e(TAG,"sendMessageToUnity error: ",e);
        }
    }

    ////////////////// Interstitial Ad Callback //////////////////
    @Override
    public void onInterstitialLoaded(String placement) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.INTERSTITIAL_AD_LOADED);
        ChocolateLogger.d(TAG, FreestarConstants.INTERSTITIAL_AD_LOADED+" Placement: "+placement);
    }

    @Override
    public void onInterstitialFailed(String placement, int errorCode) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.INTERSTITIAL_AD_FAILED);
        ChocolateLogger.d(TAG, FreestarConstants.INTERSTITIAL_AD_FAILED+" Placement: "+placement
                + " Error: " + ErrorCodes.getErrorDescription(errorCode));
    }

    @Override
    public void onInterstitialShown(String placement) {
        sendMessageToUnity(placement, FreestarConstants.INTERSTITIAL_AD_SHOWN);
        ChocolateLogger.d(TAG, FreestarConstants.INTERSTITIAL_AD_SHOWN+" Placement: "+placement);
    }

    @Override
    public void onInterstitialClicked(String placement) {
        sendMessageToUnity(placement, FreestarConstants.INTERSTITIAL_AD_CLICKED);
        ChocolateLogger.d(TAG, FreestarConstants.INTERSTITIAL_AD_CLICKED+" Placement: "+placement);
    }

    @Override
    public void onInterstitialDismissed(String placement) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.INTERSTITIAL_AD_DISMISSED);
        ChocolateLogger.d(TAG, FreestarConstants.INTERSTITIAL_AD_DISMISSED+" Placement: "+placement);
    }

    ////////////////// Reward Ad Callback //////////////////
    @Override
    public void onRewardedVideoLoaded(String placement) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.REWARDED_AD_LOADED);
        ChocolateLogger.d(TAG, FreestarConstants.REWARDED_AD_LOADED+" Placement: "+placement);
    }

    @Override
    public void onRewardedVideoFailed(String placement, int errorCode) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.REWARDED_AD_FAILED);
        ChocolateLogger.d(TAG, FreestarConstants.REWARDED_AD_FAILED+" Placement: "+placement
                + " Error: " + ErrorCodes.getErrorDescription(errorCode));
    }

    @Override
    public void onRewardedVideoShown(String placement) {
        sendMessageToUnity(placement, FreestarConstants.REWARDED_AD_SHOWN);
        ChocolateLogger.d(TAG, FreestarConstants.REWARDED_AD_SHOWN+" Placement: "+placement);
    }

    @Override
    public void onRewardedVideoShownError(String placement, int errorCode) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.REWARDED_AD_SHOWN_ERROR);
        ChocolateLogger.d(TAG, FreestarConstants.REWARDED_AD_SHOWN_ERROR+" Placement: "+placement);
    }

    @Override
    public void onRewardedVideoDismissed(String placement) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.REWARDED_AD_DISMISSED);
        ChocolateLogger.d(TAG, FreestarConstants.REWARDED_AD_DISMISSED+" Placement: "+placement);
    }

    @Override
    public void onRewardedVideoCompleted(String placement) {
        FreestarPlugin.resetFullscreenRequest();
        sendMessageToUnity(placement, FreestarConstants.REWARDED_AD_COMPLETED);
        ChocolateLogger.d(TAG, FreestarConstants.REWARDED_AD_COMPLETED+" Placement: "+placement);
    }

    @Override
    public void onError(String message) {
        sendMessageToUnity("", FreestarConstants.FREESTAR_FAILED_TO_INITIALIZE);
        ChocolateLogger.d(TAG, "initialization failed: " + message);
    }

    @Override
    public void onSuccess() {
        sendMessageToUnity("", FreestarConstants.FREESTAR_SUCCESSFULLY_INITIALIZED);
        ChocolateLogger.d(TAG, "initialized successfully");
    }

    @Override
    public void onBannerAdLoaded(View bannerAdView, String placement) {
        FreestarPlugin.resetBannerAdRequest(adSize);
        sendMessageToUnity(placement, FreestarConstants.BANNER_AD_SHOWING);
        ChocolateLogger.d(TAG, FreestarConstants.BANNER_AD_SHOWING+" Placement: "+placement);
    }

    @Override
    public void onBannerAdFailed(View bannerAdView, String placement, int errorCode) {
        FreestarPlugin.resetBannerAdRequest(adSize);
        sendMessageToUnity(placement, FreestarConstants.BANNER_AD_FAILED);
        ChocolateLogger.d(TAG, FreestarConstants.BANNER_AD_FAILED+" Placement: "+placement
                + " Error: " + ErrorCodes.getErrorDescription(errorCode));
    }

    @Override
    public void onBannerAdClicked(View bannerAdView, String placement) {
        sendMessageToUnity(placement, FreestarConstants.BANNER_AD_CLICKED);
        ChocolateLogger.d(TAG, FreestarConstants.BANNER_AD_CLICKED+" Placement: "+placement);
    }

    @Override
    public void onBannerAdClosed(View bannerAdView, String placement) {
        //not implemented
    }
}
