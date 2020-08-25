package com.vdopia.unity.plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.freestar.android.ads.AdRequest;
import com.freestar.android.ads.ChocolateLogger;
import com.freestar.android.ads.FreeStarAds;
import com.freestar.android.ads.InterstitialAd;
import com.freestar.android.ads.RewardedAd;
import com.unity3d.player.UnityPlayer;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class VdopiaPlugin {

    private static final String TAG = "VdopiaUnityPlugin";

    private static final boolean DEBUG_SHOW_CHOOSER = false;
    private Activity mActivity;
    private VdopiaAdUnityListener mUnityAdListener;

    private AdRequest mAdRequest;
    private RewardedAd mRewardedAd;
    private InterstitialAd mInterstitialAd;

    private static boolean isRequestInProgress;
    private static long lastRequestTime;

    private static VdopiaPlugin VdopiaPluginInstance;

    private VdopiaPlugin() {
        //Empty Private Constructor
    }

    public static synchronized VdopiaPlugin GetInstance() {
        //ChocolateLogger.enable(true);
        Log.i(TAG, "Unity Plugin Set.");
        if (VdopiaPluginInstance == null) {
            VdopiaPluginInstance = new VdopiaPlugin();
        }

        return VdopiaPluginInstance;
    }

    private AdRequest getAdRequest() {
        if (mAdRequest == null) {
            mAdRequest = new AdRequest(getActivity());
        }
        return mAdRequest;
    }

    /**
     * This is the first method that is called on the single instance of VdopiaPlugin.
     *
     * @param activity
     */
    public void SetActivity(Activity activity) {
        Log.i(TAG, "Unity Activity Set.");
        mActivity = activity;
        getAdRequest();
    }

    public void SetUnityAdListener(VdopiaAdUnityListener listener) {
        Log.i(TAG, "Unity Listener Set.");
        mUnityAdListener = listener;
    }

    public void SetAdRequestUserParams(String age, String birthDate, String gender, String maritalStatus,
                                       String ethnicity, String dmaCode, String postal, String curPostal,
                                       String latitude, String longitude) {
        Log.i(TAG, "Unity User Params Set.");
        getAdRequest();

        try {
            mAdRequest.setAge(age);
            mAdRequest.setBirthday(getDate(birthDate));
        } catch (Exception e) {
            ChocolateLogger.e(TAG,"SetAdRequestUserParams (1):",e);
        }

        try {
            if (TextUtils.isEmpty(gender)) {
                gender = "";
            }
            mAdRequest.setGender(gender);

            if (TextUtils.isEmpty(maritalStatus)) {
                maritalStatus = "";
            }
            mAdRequest.setMaritalStatus(maritalStatus);

            mAdRequest.setEthnicity(ethnicity);
            mAdRequest.setDmaCode(dmaCode);
            mAdRequest.setPostalCode(postal);
            mAdRequest.setCurrPostal(curPostal);
        }catch (Exception e) {
            ChocolateLogger.e(TAG,"SetAdRequestUserParams (2):",e);
        }

        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
            try {
                Log.i(TAG, "Unity Location Params Set.");
                Location location = new Location("");
                location.setLatitude(Double.valueOf(latitude));
                location.setLongitude(Double.valueOf(longitude));
                mAdRequest.setLocation(location);
            } catch (Exception e) {
                ChocolateLogger.e(TAG, "Unity Location Invalid: " + e);
            }
        }
    }

    public void SetAdRequestAppParams(String appName, String pubName,
                                      String appDomain, String pubDomain,
                                      String storeUrl, String iabCategory) {
        Log.i(TAG, "Unity App Params Set.");
        getAdRequest();

        mAdRequest.setAppName(appName);
        mAdRequest.setRequester(pubName);

        mAdRequest.setAppDomain(appDomain);
        mAdRequest.setPublisherDomain(pubDomain);

        mAdRequest.setAppStoreUrl(storeUrl);
        mAdRequest.setCategory(iabCategory);
    }

    public void SetTestModeEnabled(boolean isEnabled, String hashID) {
        hashID = hashID == null ? "TEST-HASH-ID-NOT-SET-OK" : hashID;
        Log.i(TAG, "Unity Test Params Set: " + isEnabled);
        ChocolateLogger.enable(isEnabled);
        FreeStarAds.enableTestAds(isEnabled);
        getAdRequest();
        mAdRequest.setTestModeEnabled(isEnabled);

        Set<String> testID = new HashSet<>();
        testID.add(hashID);
        mAdRequest.setTestDevices(testID);
    }

    public void ChocolateInit(final String apiKey) {
        Log.i(TAG, "ChocolateInit: " + apiKey);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    FreeStarAds.init(getActivity(), apiKey);
                } catch (Throwable t) {
                    Log.e(TAG,"ChocolateInit failed",t);
                }
            }
        });
    }

    public void PrefetchInterstitialAd(final String apiKey) {
        //does nothing, but don't delete; it could break publishers code
    }

    private boolean isQuitting() {
        return getActivity() == null || getActivity().isFinishing();
    }

    public void LoadInterstitialAd(final String apiKey) {
        if (DEBUG_SHOW_CHOOSER) {
            ChocolatePartners.choosePartners(ChocolatePartners.ADTYPE_INTERSTITIAL, getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ChocolatePartners.setInterstitialPartners(getAdRequest());
                    _LoadInterstitialAd(apiKey);
                }
            });
        } else {
            _LoadInterstitialAd(apiKey);
        }
    }

    private void _LoadInterstitialAd(final String apiKey) {

        if (isQuitting()) return;
        if (!canRequest()) {
            Log.i(TAG, "Cannot LoadInterstitialAd while another ad is in progress");
            return;
        }
        markRequest();
        Log.i(TAG, "LoadInterstitialAd Called.");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mInterstitialAd == null)
                        mInterstitialAd = new InterstitialAd(getActivity(), new VdopiaSdkAdEventHandler(VdopiaSdkAdEventHandler.INTERSTITIAL_AD_TYPE, mUnityAdListener));
                    mInterstitialAd.loadAd(getAdRequest());
                } catch (Exception e) {
                    Log.e(TAG,"LoadInterstitialAd failed",e);
                    /*
                    Let's hold off on making the failed callback since we weren't doing it before.

                    if (mUnityAdListener != null) {
                        mUnityAdListener.onVdopiaAdEvent(VdopiaSdkAdEventHandler.INTERSTITIAL_AD_TYPE, VdopiaSdkAdEventHandler.INTERSTITIAL_AD_FAILED);
                    }
                    */
                    resetRequest();
                }
            }
        });
    }

    public void ShowInterstitialAd() {
        Log.i(TAG, "ShowInterstitialAd Called.");
        if (isQuitting()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd != null) {
                    try {
                        mInterstitialAd.show();
                    } catch (Exception e) {
                        ChocolateLogger.e(TAG, "ShowInterstitialAd failed", e);
                        resetRequest();
                    }
                } else {
                    Log.i(TAG, "ShowInterstitialAd Null");
                    resetRequest();
                }
            }
        });
    }

    public void PrefetchRewardAd(final String apiKey) {
        //does nothing, but don't delete; it could break publishers code
    }

    public void LoadRewardAd(final String apiKey) {
        if (DEBUG_SHOW_CHOOSER) {
            ChocolatePartners.choosePartners(ChocolatePartners.ADTYPE_REWARDED, getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ChocolatePartners.setRewardedPartners(getAdRequest());
                    _LoadRewardAd(apiKey);
                }
            });
        } else {
            _LoadRewardAd(apiKey);
        }
    }

    private void _LoadRewardAd(final String apiKey) {

        if (isQuitting()) return;
        if (!canRequest()) {
            Log.i(TAG, "Cannot LoadRewardAd while another ad is in progress");
            return;
        }
        markRequest();
        Log.i(TAG, "LoadRewardAd Called.");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mRewardedAd == null)
                        mRewardedAd = new RewardedAd(getActivity(), new VdopiaSdkAdEventHandler(VdopiaSdkAdEventHandler.REWARD_AD_TYPE, mUnityAdListener));
                    mRewardedAd.loadAd(getAdRequest());
                }catch (Exception e) {
                    ChocolateLogger.e(TAG,"LoadRewardAd failed",e);
                    /*
                    Let's hold off on making the failed callback since we weren't doing it before.

                    if (mUnityAdListener != null) {
                        mUnityAdListener.onVdopiaAdEvent(VdopiaSdkAdEventHandler.REWARD_AD_TYPE, VdopiaSdkAdEventHandler.REWARD_AD_FAILED);
                    }
                    */
                    resetRequest();
                }
            }
        });
    }

    public boolean IsRewardAdAvailableToShow() {
        try {
            return mRewardedAd != null && mRewardedAd.isReady();
        }catch (Exception e) {
            ChocolateLogger.e(TAG,"IsRewardAdAvailableToShow failed",e);
            return false;
        }
    }

    public boolean IsInterstitialAdAvailableToShow() {
        try {
            return mInterstitialAd != null && mInterstitialAd.isReady();
        }catch (Exception e) {
            ChocolateLogger.e(TAG,"IsInterstitialAdAvailableToShow failed",e);
            return false;
        }
    }

    public void ShowRewardAd(final String secretCode, final String userID,
                             final String rewardName, final String rewardAmount) {
        Log.d(TAG, "Reward To : " + userID + " " + rewardAmount + " " + rewardName);
        if (isQuitting()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    if (mRewardedAd != null && mRewardedAd.isReady()) {
                        mRewardedAd.showRewardAd(secretCode, userID, rewardName, rewardAmount);
                        return;
                    }

                    if (mRewardedAd == null) {
                        mRewardedAd = new RewardedAd(getActivity(), new VdopiaSdkAdEventHandler(VdopiaSdkAdEventHandler.REWARD_AD_TYPE, new VdopiaAdUnityListener() {
                            @Override
                            public void onVdopiaAdEvent(String adType, String adEvent) {
                                if (adEvent != null && adEvent.equals("REWARD_AD_LOADED")) {
                                    mRewardedAd.showRewardAd(secretCode, userID, rewardName, rewardAmount);
                                }
                                resetRequest();
                            }
                        }));
                    }
                    mRewardedAd.loadAd(getAdRequest());
                }catch (Exception e) {
                    ChocolateLogger.e(TAG,"ShowRewardAd failed",e);
                    resetRequest();
                }
            }
        });
    }

    private Activity getActivity() {
        if (mActivity == null) {
            mActivity = UnityPlayer.currentActivity;
        }
        return mActivity;
    }

    private Date getDate(String dateString) {
        try {
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            return df.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    static void resetRequest() {
        isRequestInProgress = false;
        lastRequestTime = 0;
    }

    private void markRequest() {
        isRequestInProgress = true;
        lastRequestTime = System.currentTimeMillis();
    }

    private boolean canRequest() {
        if (lastRequestTime == 0 || (System.currentTimeMillis() - lastRequestTime > 60000L)) {
            if (isRequestInProgress) {
                resetRequest();
            }
        }
        return !isRequestInProgress;
    }

    public void SetGDPR(final String isSubjectToGDPR, final String iabConsentString) {
        if (isQuitting()) return;
        try {
            FreeStarAds.setGDPR(getActivity(), isSubjectToGDPR != null && isSubjectToGDPR.equals("1"), iabConsentString);
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "SetGDPR() failed", e);
        }
    }

    public boolean IsSubjectToGDPR() {
        try {
            return FreeStarAds.isSubjectToGDPR(getActivity());
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "IsSubjectToGDPR failed", e);
        }
        return false;
    }

    public boolean IsGDPRConsentAvailable() {
        try {
            return FreeStarAds.getGDPRConsentString(getActivity()) != null;
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "IsGDPRConsentAvailable failed", e);
        }
        return false;
    }

    public String GetGDPRConsentString() {
        try {
            return FreeStarAds.getGDPRConsentString(getActivity());
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "GetGDPRConsentString failed", e);
        }
        return null;
    }

    public void SetUserId(String userId) {
        try {
            FreeStarAds.setUserId(getActivity(), userId);
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "SetUserId failed", e);
        }
    }

    public void SetPrivacySettings(boolean gdprApplies, String gdprConsentString) {
        try {
            FreeStarAds.setGDPR(getActivity(), gdprApplies, gdprConsentString);
        }catch (Throwable t) {
            ChocolateLogger.e(TAG, "SetPrivacySettings failed", t);
        }
    }

    public void SetCustomSegmentProperty(String key, String value) {
        try {
            FreeStarAds.setCustomSegmentProperty(getActivity(), key, value);
        }catch (Throwable t) {
            ChocolateLogger.e(TAG, "SetCustomSegmentProperty failed", t);
        }
    }

    public String GetCustomSegmentProperty(String key) {
        try {
            return FreeStarAds.getCustomSegmentProperty(getActivity(), key);
        }catch (Throwable t) {
            ChocolateLogger.e(TAG, "GetCustomSegmentProperty failed", t);
            return null;
        }
    }

    public String GetAllCustomSegmentProperties() {
        try {
            JSONObject allCustomProperties = FreeStarAds.getAllCustomSegmentProperties(getActivity());
            if (allCustomProperties != null) {
                return allCustomProperties.toString();
            } else {
                return null;
            }
        }catch (Throwable t) {
            ChocolateLogger.e(TAG, "GetAllCustomSegmentProperties failed", t);
            return null;
        }
    }

    public void DeleteCustomSegmentProperty(String key) {
        try {
            FreeStarAds.deleteCustomSegmentProperty(getActivity(), key);
        }catch (Throwable t) {
            ChocolateLogger.e(TAG, "DeleteCustomSegmentProperty failed", t);
        }
    }

    public void DeleteAllCustomSegmentProperties() {
        try {
            FreeStarAds.deleteAllCustomSegmentProperties(getActivity());
        }catch (Throwable t) {
            ChocolateLogger.e(TAG, "DeleteAllCustomSegmentProperties failed", t);
        }
    }

    //getAllCustomSegmentProperties

    public String GetRewardAdWinner() {
        try {
            return mRewardedAd != null && mRewardedAd.isReady() ? mRewardedAd.getWinningPartnerName() : "";
        }catch (Exception e) {
            ChocolateLogger.e(TAG, "GetRewardAdWinner failed", e);
            return "";
        }
    }

    public String GetInterstitialAdWinner() {
        try {
            return mInterstitialAd != null && mInterstitialAd.isReady() ? mInterstitialAd.getWinningPartnerName() : "";
        }catch (Exception e) {
            ChocolateLogger.e(TAG, "GetInterstitialAdWinner failed", e);
            return "";
        }
    }
}
