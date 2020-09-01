package com.freestar.android.unity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.location.Location;
import android.text.TextUtils;

import com.freestar.android.ads.AdRequest;
import com.freestar.android.ads.ChocolateLogger;
import com.freestar.android.ads.FreeStarAds;
import com.freestar.android.ads.InterstitialAd;
import com.freestar.android.ads.RewardedAd;
import com.unity3d.player.UnityPlayer;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FreestarPlugin {

    private static final String TAG = "FreestarPlugin";

    private static final boolean DEBUG_SHOW_CHOOSER = false;
    private WeakReference<Activity> mActivity;
    private FreestarAdUnityListener mUnityAdListener;

    private AdRequest mAdRequest;
    private Map<String, InterstitialAd> interstitialAdMap = new HashMap<>();
    private Map<String, RewardedAd> rewardedAdMap = new HashMap<>();

    private static boolean isRequestInProgress;
    private static long lastRequestTime;

    private static FreestarPlugin freestarPluginInstance;

    private FreestarPlugin() {
        //empty constructor
    }

    private RewardedAd getRewardedAd(String placement) {
        placement = placement + "";
        if (rewardedAdMap.containsKey(placement)) {
            return rewardedAdMap.get(placement);
        }
        RewardedAd rewardedAd = new RewardedAd(getActivity(), new FreestarAdEventHandler(FreestarAdEventHandler.REWARDED_AD_TYPE, mUnityAdListener));
        rewardedAdMap.put(placement, rewardedAd);
        return rewardedAd;
    }

    private InterstitialAd getInterstitialAd(String placement) {
        placement = placement + "";
        if (interstitialAdMap.containsKey(placement)) {
            return interstitialAdMap.get(placement);
        }
        InterstitialAd interstitialAd = new InterstitialAd(getActivity(), new FreestarAdEventHandler(FreestarAdEventHandler.INTERSTITIAL_AD_TYPE, mUnityAdListener));
        interstitialAdMap.put(placement, interstitialAd);
        return interstitialAd;
    }

    public static synchronized FreestarPlugin GetInstance() {
        ChocolateLogger.i(TAG, "Unity Plugin Set.");
        if (freestarPluginInstance == null) {
            freestarPluginInstance = new FreestarPlugin();
        }

        return freestarPluginInstance;
    }

    private AdRequest getAdRequest() {
        if (mAdRequest == null) {
            mAdRequest = new AdRequest(getActivity());
        }
        return mAdRequest;
    }

    /**
     * This is the first method that is called on the single instance of FreestarPlugin.
     *
     * @param activity
     */
    public void SetActivity(Activity activity) {
        ChocolateLogger.i(TAG, "Unity Activity Set.");
        mActivity = new WeakReference<>(activity);
        getAdRequest();
    }

    public void SetUnityAdListener(FreestarAdUnityListener listener) {
        ChocolateLogger.i(TAG, "Unity Listener Set.");
        mUnityAdListener = listener;
    }

    public void SetAdRequestUserParams(String age, String birthDate, String gender, String maritalStatus,
                                       String ethnicity, String dmaCode, String postal, String curPostal,
                                       String latitude, String longitude) {
        ChocolateLogger.i(TAG, "Unity User Params Set.");
        getAdRequest();

        try {
            mAdRequest.setAge(age);
            mAdRequest.setBirthday(getDate(birthDate));
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "SetAdRequestUserParams (1):", e);
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
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "SetAdRequestUserParams (2):", e);
        }

        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
            try {
                ChocolateLogger.i(TAG, "Unity Location Params Set.");
                Location location = new Location("");
                location.setLatitude(Double.valueOf(latitude));
                location.setLongitude(Double.valueOf(longitude));
                mAdRequest.setLocation(location);
            } catch (Exception e) {
                ChocolateLogger.e(TAG, "Unity Location Invalid: " + e);
            }
        }
    }

    public void SetTestModeEnabled(boolean isEnabled, String hashID) {
        ChocolateLogger.i(TAG, "Unity Test Params Set: " + isEnabled);
        FreeStarAds.enableLogging(isEnabled);
        FreeStarAds.enableTestAds(isEnabled);
        getAdRequest().setTestModeEnabled(isEnabled);

        if (!TextUtils.isEmpty(hashID)) {
            Set<String> testID = new HashSet<>();
            testID.add(hashID);
            mAdRequest.setTestDevices(testID);
        }
    }

    public void Init(final String apiKey) {
        ChocolateLogger.i(TAG, "Init: " + apiKey);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    FreeStarAds.init(getActivity(), apiKey);
                } catch (Throwable t) {
                    ChocolateLogger.e(TAG, "Init failed", t);
                }
            }
        });
    }

    private boolean isQuitting() {
        return getActivity() == null || getActivity().isFinishing();
    }

    public void LoadInterstitialAd(final String apiKey) {
        if (DEBUG_SHOW_CHOOSER) {
            MediationPartners.choosePartners(MediationPartners.ADTYPE_INTERSTITIAL, getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MediationPartners.setInterstitialPartners(getAdRequest());
                    _LoadInterstitialAd(apiKey);
                }
            });
        } else {
            _LoadInterstitialAd(apiKey);
        }
    }

    private void _LoadInterstitialAd(final String placement) {

        if (isQuitting()) return;
        if (!canRequest()) {
            ChocolateLogger.i(TAG, "Cannot LoadInterstitialAd while another ad is in progress");
            return;
        }
        markRequest();
        ChocolateLogger.i(TAG, "LoadInterstitialAd Called.");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getInterstitialAd(placement).loadAd(getAdRequest(), placement);
            }
        });
    }

    private InterstitialAd tempInterstitialAd;

    public void ShowInterstitialAd(final String placement) {
        if (isQuitting()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getInterstitialAd(placement).isReady()) {
                    ChocolateLogger.i(TAG, "ShowInterstitialAd (a)");
                    getInterstitialAd(placement).show();
                } else {
                    tempInterstitialAd = new InterstitialAd(getActivity(), new FreestarAdEventHandler(FreestarAdEventHandler.INTERSTITIAL_AD_TYPE, new FreestarAdUnityListener() {
                        @Override
                        public void onFreestarAdEvent(String placement, String adType, String adEvent) {
                            if (isQuitting()) return;
                            if (adEvent.equals(FreestarAdEventHandler.INTERSTITIAL_AD_LOADED)) {
                                tempInterstitialAd.show();
                            }
                        }
                    }));
                    ChocolateLogger.i(TAG, "ShowInterstitialAd (b)");
                    interstitialAdMap.put(placement + "", tempInterstitialAd);
                    tempInterstitialAd.loadAd(getAdRequest(), placement);
                }
            }
        });
    }

    public void LoadRewardAd(final String placement) {
        if (DEBUG_SHOW_CHOOSER) {
            MediationPartners.choosePartners(MediationPartners.ADTYPE_REWARDED, getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MediationPartners.setRewardedPartners(getAdRequest());
                    _LoadRewardAd(placement);
                }
            });
        } else {
            _LoadRewardAd(placement);
        }
    }

    private void _LoadRewardAd(final String placement) {

        if (isQuitting()) return;
        if (!canRequest()) {
            ChocolateLogger.i(TAG, "Cannot LoadRewardAd while another ad is in progress");
            return;
        }
        markRequest();
        ChocolateLogger.i(TAG, "LoadRewardAd Called.");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getRewardedAd(placement).loadAd(getAdRequest(), placement);
            }
        });
    }

    public boolean IsRewardAdAvailableToShow(String placement) {
        try {
            return getRewardedAd(placement).isReady();
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "IsRewardAdAvailableToShow failed", e);
            return false;
        }
    }

    public boolean IsInterstitialAdAvailableToShow(String placement) {
        try {
            return getInterstitialAd(placement).isReady();
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "IsInterstitialAdAvailableToShow failed", e);
            return false;
        }
    }

    private RewardedAd tempRewardedAd;

    public void ShowRewardAd(final String placement,
                             final String secretCode, final String userID,
                             final String rewardName, final String rewardAmount) {
        ChocolateLogger.d(TAG, "Reward To : " + userID + " " + rewardAmount + " " + rewardName);
        if (isQuitting()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (getRewardedAd(placement).isReady()) {
                    ChocolateLogger.i(TAG, "ShowRewardAd (a)");
                    getRewardedAd(placement).showRewardAd(secretCode, userID, rewardName, rewardAmount);
                } else {
                    tempRewardedAd = new RewardedAd(getActivity(), new FreestarAdEventHandler(FreestarAdEventHandler.REWARDED_AD_TYPE, new FreestarAdUnityListener() {
                        @Override
                        public void onFreestarAdEvent(String placement, String adType, String adEvent) {
                            if (isQuitting()) return;
                            if (adEvent.equals(FreestarAdEventHandler.REWARD_AD_LOADED)) {
                                tempRewardedAd.showRewardAd(secretCode, userID, rewardName, rewardAmount);
                            }
                        }
                    }));
                    ChocolateLogger.i(TAG, "ShowRewardAd (b)");
                    rewardedAdMap.put(placement + "", tempRewardedAd);
                    tempRewardedAd.loadAd(getAdRequest(), placement);
                }
            }
        });
    }

    private Activity getActivity() {
        if (mActivity == null) {
            mActivity = new WeakReference<>(UnityPlayer.currentActivity);
        }
        return mActivity.get();
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
        } catch (Throwable t) {
            ChocolateLogger.e(TAG, "SetPrivacySettings failed", t);
        }
    }

    public void SetCustomSegmentProperty(String key, String value) {
        try {
            FreeStarAds.setCustomSegmentProperty(getActivity(), key, value);
        } catch (Throwable t) {
            ChocolateLogger.e(TAG, "SetCustomSegmentProperty failed", t);
        }
    }

    public String GetCustomSegmentProperty(String key) {
        try {
            return FreeStarAds.getCustomSegmentProperty(getActivity(), key);
        } catch (Throwable t) {
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
        } catch (Throwable t) {
            ChocolateLogger.e(TAG, "GetAllCustomSegmentProperties failed", t);
            return null;
        }
    }

    public void DeleteCustomSegmentProperty(String key) {
        try {
            FreeStarAds.deleteCustomSegmentProperty(getActivity(), key);
        } catch (Throwable t) {
            ChocolateLogger.e(TAG, "DeleteCustomSegmentProperty failed", t);
        }
    }

    public void DeleteAllCustomSegmentProperties() {
        try {
            FreeStarAds.deleteAllCustomSegmentProperties(getActivity());
        } catch (Throwable t) {
            ChocolateLogger.e(TAG, "DeleteAllCustomSegmentProperties failed", t);
        }
    }

    public String GetRewardAdWinner(String placement) {
        try {
            return getRewardedAd(placement).getWinningPartnerName();
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "GetRewardAdWinner failed", e);
            return "";
        }
    }

    public String GetInterstitialAdWinner(String placement) {
        try {
            return getInterstitialAd(placement).getWinningPartnerName();
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "GetInterstitialAdWinner failed", e);
            return "";
        }
    }
}
