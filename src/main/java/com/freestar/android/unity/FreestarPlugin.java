package com.freestar.android.unity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.location.Location;
import android.text.TextUtils;
import android.view.View;

import com.freestar.android.ads.AdRequest;
import com.freestar.android.ads.AdSize;
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

    private static boolean SHOW_PARTNER_CHOOSER = false;
    private WeakReference<Activity> activity;
    private FreestarAdUnityListener unityAdListener;

    private AdRequest adRequest;
    private Map<String, InterstitialAd> interstitialAdMap = new HashMap<>();
    private Map<String, RewardedAd> rewardedAdMap = new HashMap<>();
    private Map<String, PopupBannerAd> bannerAdMap = new HashMap<>();

    private static boolean isRequestInProgress;
    private static long lastRequestTime;
    private static boolean smallBannerInProgress;
    private static boolean mrecBannerInProgress;

    private static FreestarPlugin freestarPluginInstance;

    private FreestarPlugin() {
        //empty constructor
    }

    private RewardedAd getRewardedAd(String placement) {
        placement = placement + "";
        if (rewardedAdMap.containsKey(placement)) {
            return rewardedAdMap.get(placement);
        }
        RewardedAd rewardedAd = new RewardedAd(getActivity(),
                new FreestarAdEventHandler(FreestarConstants.REWARDED_AD_TYPE,
                        FreestarConstants.FULLSCREEN_AD_SIZE, unityAdListener));
        rewardedAdMap.put(placement, rewardedAd);
        return rewardedAd;
    }

    private PopupBannerAd getBannerAd(String placement, int adSize) {
        placement = placement + "";
        if (bannerAdMap.containsKey(placement+adSize)) {
            return bannerAdMap.get(placement+adSize);
        }
        PopupBannerAd bannerAd = new PopupBannerAd(getActivity());
        bannerAdMap.put(placement+adSize, bannerAd);
        return bannerAd;
    }

    private InterstitialAd getInterstitialAd(String placement) {
        placement = placement + "";
        if (interstitialAdMap.containsKey(placement)) {
            return interstitialAdMap.get(placement);
        }
        InterstitialAd interstitialAd = new InterstitialAd(getActivity(),
                new FreestarAdEventHandler(FreestarConstants.INTERSTITIAL_AD_TYPE,
                        FreestarConstants.FULLSCREEN_AD_SIZE,
                        unityAdListener));
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
        if (adRequest == null) {
            adRequest = new AdRequest(getActivity());
        }
        return adRequest;
    }

    public void ShowPartnerChooser(boolean showPartnerChooser) {
        SHOW_PARTNER_CHOOSER = showPartnerChooser;
    }

    /**
     * This is the first method that is called on the single instance of FreestarPlugin.
     *
     * @param activity
     */
    public void SetActivity(Activity activity) {
        ChocolateLogger.i(TAG, "Unity Activity Set.");
        this.activity = new WeakReference<>(activity);
        getAdRequest();
    }

    public void SetUnityAdListener(FreestarAdUnityListener listener) {
        ChocolateLogger.i(TAG, "Unity Listener Set.");
        unityAdListener = listener;
    }

    public void SetAdRequestUserParams(String age, String birthDate, String gender, String maritalStatus,
                                       String ethnicity, String dmaCode, String postal, String curPostal,
                                       String latitude, String longitude) {
        ChocolateLogger.i(TAG, "Unity User Params Set.");
        getAdRequest();

        try {
            adRequest.setAge(age);
            adRequest.setBirthday(getDate(birthDate));
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "SetAdRequestUserParams (1):", e);
        }

        try {
            if (TextUtils.isEmpty(gender)) {
                gender = "";
            }
            adRequest.setGender(gender);

            if (TextUtils.isEmpty(maritalStatus)) {
                maritalStatus = "";
            }
            adRequest.setMaritalStatus(maritalStatus);

            adRequest.setEthnicity(ethnicity);
            adRequest.setDmaCode(dmaCode);
            adRequest.setPostalCode(postal);
            adRequest.setCurrPostal(curPostal);
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "SetAdRequestUserParams (2):", e);
        }

        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
            try {
                ChocolateLogger.i(TAG, "Unity Location Params Set.");
                Location location = new Location("");
                location.setLatitude(Double.valueOf(latitude));
                location.setLongitude(Double.valueOf(longitude));
                adRequest.setLocation(location);
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
            adRequest.setTestDevices(testID);
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

    public void LoadInterstitialAd(final String placement) {
        if (SHOW_PARTNER_CHOOSER) {
            MediationPartners.choosePartners(getActivity(), getAdRequest(), MediationPartners.ADTYPE_INTERSTITIAL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _LoadInterstitialAd(placement);
                }
            });
        } else {
            _LoadInterstitialAd(placement);
        }
    }

    private void _LoadInterstitialAd(final String placement) {

        if (isQuitting()) return;
        if (!canFullscreenRequest()) {
            ChocolateLogger.i(TAG, "Cannot LoadInterstitialAd while another ad is in progress");
            return;
        }
        markFullscreenRequest();
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
                    tempInterstitialAd = new InterstitialAd(getActivity(),
                    new FreestarAdEventHandler(
                        FreestarConstants.INTERSTITIAL_AD_TYPE,
                        FreestarConstants.FULLSCREEN_AD_SIZE,
                        new FreestarAdUnityListener() {
                            @Override
                            public void onFreestarAdEvent(String placement, String adType, int adSize, String adEvent) {
                                if (isQuitting()) return;
                                if (adEvent.equals(FreestarConstants.INTERSTITIAL_AD_LOADED)) {
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

    public void LoadRewardedAd(final String placement) {
        if (SHOW_PARTNER_CHOOSER) {
            MediationPartners.choosePartners(getActivity(), getAdRequest(), MediationPartners.ADTYPE_REWARDED, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _LoadRewardedAd(placement);
                }
            });
        } else {
            _LoadRewardedAd(placement);
        }
    }

    private void _LoadRewardedAd(final String placement) {

        if (isQuitting()) return;
        if (!canFullscreenRequest()) {
            ChocolateLogger.i(TAG, "Cannot LoadRewardedAd while another ad is in progress");
            return;
        }
        markFullscreenRequest();
        ChocolateLogger.i(TAG, "LoadRewardedAd Called.");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getRewardedAd(placement).loadAd(getAdRequest(), placement);
            }
        });
    }

    public boolean IsRewardedAdAvailableToShow(String placement) {
        try {
            return getRewardedAd(placement).isReady();
        } catch (Exception e) {
            ChocolateLogger.e(TAG, "IsRewardedAdAvailableToShow failed", e);
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

    public void ShowRewardedAd(final String placement,
                               final String secretCode, final String userID,
                               final String rewardName, final String rewardAmount) {
        ChocolateLogger.d(TAG, "Reward To : " + userID + " " + rewardAmount + " " + rewardName);
        if (isQuitting()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (getRewardedAd(placement).isReady()) {
                    ChocolateLogger.i(TAG, "ShowRewardedAd (a)");
                    getRewardedAd(placement).showRewardAd(secretCode, userID, rewardName, rewardAmount);
                } else {
                    tempRewardedAd = new RewardedAd(getActivity(),
                            new FreestarAdEventHandler(
                                    FreestarConstants.REWARDED_AD_TYPE,
                                    FreestarConstants.FULLSCREEN_AD_SIZE,
                                    new FreestarAdUnityListener() {
                                        @Override
                                        public void onFreestarAdEvent(String placement, String adType, int adSize, String adEvent) {
                                            if (isQuitting()) return;
                                            if (adEvent.equals(FreestarConstants.REWARDED_AD_LOADED)) {
                                                tempRewardedAd.showRewardAd(secretCode, userID, rewardName, rewardAmount);
                                            }
                                        }
                                    }));
                    ChocolateLogger.i(TAG, "ShowRewardedAd (b)");
                    rewardedAdMap.put(placement + "", tempRewardedAd);
                    tempRewardedAd.loadAd(getAdRequest(), placement);
                }
            }
        });
    }

    private Activity getActivity() {
        if (activity == null) {
            activity = new WeakReference<>(UnityPlayer.currentActivity);
        }
        return activity.get();
    }

    private Date getDate(String dateString) {
        try {
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            return df.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    static void resetFullscreenRequest() {
        isRequestInProgress = false;
        lastRequestTime = 0;
    }

    static void resetBannerAdRequest(int bannerAdSize) {
        if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_320x50) {
            smallBannerInProgress = false;
        }
        if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_300x250) {
            mrecBannerInProgress = false;
        }
    }

    private void markFullscreenRequest() {
        isRequestInProgress = true;
        lastRequestTime = System.currentTimeMillis();
    }

    private boolean canFullscreenRequest() {
        if (lastRequestTime == 0 || (System.currentTimeMillis() - lastRequestTime > 60000L)) {
            if (isRequestInProgress) {
                resetFullscreenRequest();
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

    public String GetRewardedAdWinner(String placement) {
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

    public void ShowBannerAd(final String placement, final int bannerAdSize, final int bannerAdPosition) {
        if (SHOW_PARTNER_CHOOSER) {
            MediationPartners.choosePartners(getActivity(), getAdRequest(), MediationPartners.ADTYPE_INVIEW, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _ShowBannerAd(placement, bannerAdSize, bannerAdPosition);
                }
            });
        } else {
            _ShowBannerAd(placement, bannerAdSize, bannerAdPosition);
        }
    }

    /**
     * @param placement
     * @param bannerAdSize
     * @param bannerAdPosition
     */
    private void _ShowBannerAd(String placement, int bannerAdSize, int bannerAdPosition) {

        ChocolateLogger.i(TAG,"_ShowBannerAd bannerAdSize: " + bannerAdSize);

        if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_300x250 && mrecBannerInProgress) {
            return; //already in progress
        }

        if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_320x50 && smallBannerInProgress) {
            return; //already in progress
        }

        if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_300x250) {
            mrecBannerInProgress = true;
        }

        if (bannerAdSize == FreestarConstants.BANNER_AD_SIZE_320x50) {
            smallBannerInProgress = true;
        }

        if (IsBannerAdShowing(placement, bannerAdSize)) {
            CloseBannerAd(placement, bannerAdSize);
        }

        final PopupBannerAd bannerAd = getBannerAd(placement, bannerAdSize);
        AdSize adSize = bannerAdSize == FreestarConstants.BANNER_AD_SIZE_300x250 ? AdSize.MEDIUM_RECTANGLE_300_250
                : AdSize.BANNER_320_50;
        bannerAd.loadBannerAd(getAdRequest(), adSize, placement, bannerAdPosition,
                new FreestarAdEventHandler(
                        FreestarConstants.BANNER_AD_TYPE,
                        bannerAdSize,
                        unityAdListener)
                        {
                            @Override
                            public void onBannerAdLoaded(View bannerAdView, String placement) {
                                bannerAd.showBannerAd(bannerAdView);
                                super.onBannerAdLoaded(bannerAdView, placement);
                            }
                        });
    }

    public boolean IsBannerAdShowing(String placement, int adSize) {
        if (bannerAdMap.containsKey(placement + adSize)) {
            return bannerAdMap.get(placement + adSize).isShowing();
        }
        return false;
    }

    public void CloseBannerAd(String placement, int adSize) {
        if (bannerAdMap.containsKey(placement + adSize)) {
            if (bannerAdMap.get(placement + adSize).isShowing()) {
                bannerAdMap.get(placement + adSize).destroy();
            }
            bannerAdMap.remove(placement + adSize);
        }
    }

    public void OnResume() {
        for (String key : bannerAdMap.keySet()) {
            bannerAdMap.get(key).onResume();
        }
    }

    public void OnPause() {
        for (String key : bannerAdMap.keySet()) {
            bannerAdMap.get(key).onPause();
        }
    }
}
