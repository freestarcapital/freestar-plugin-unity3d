﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Runtime.InteropServices;
using System;

namespace Freestar
{

    public interface FreestarInterstitialAdCallbackReceiver
    {
        void onInterstitialAdLoaded(string placement);
        void onInterstitialAdFailed(string placement);
        void onInterstitialAdShown(string placement);
        void onInterstitialAdClicked(string placement);
        void onInterstitialAdDismissed(string placement);
    }

    public interface FreestarRewardedAdCallbackReceiver
    {
        void onRewardedAdLoaded(string placement);
        void onRewardedAdFailed(string placement);
        void onRewardedAdShown(string placement);
        void onRewardedAdFinished(string placement);
        void onRewardedAdDismissed(string placement);
    }

    public interface FreestarBannerAdCallbackReceiver
    {
        void onBannerAdShowing(string placement, int adSize);
        void onBannerAdClicked(string placement, int adSize);
        void onBannerAdFailed(string placement, int adSize);
    }

    public class FreestarUnityBridge : MonoBehaviour
    {

#if UNITY_IOS

         [System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _initWithAPIKey(string apiKey);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setInterstitialListener(string listenerName);

         [System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setRewardedListener(string listenerName);

         [System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setBannerListener(string listenerName);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _loadInterstitialAd(string placement);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _showInterstitialAd(string placement);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _loadRewardedAd(string placement);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _showRewardedAd(string placement, int rewardAmount, string rewardName, string userId, string secretKey);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setDemograpics(int age, string birthDate, string gender, string maritalStatus,
    		                     string ethnicity);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setLocation(string dmaCode, string postal, string curPostal, string latitude, string longitude);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setCustomSegmentProperty(string key, string value);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public string _getCustomSegmentProperty(string key);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public string _getAllCustomSegmentProperties();

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _deleteCustomSegmentProperty(string key);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _deleteAllCustomSegmentProperties();
    		
         [System.Runtime.InteropServices.DllImport("__Internal")]
         extern static public void _showBannerAd(string placement, int bannerAdSize, int bannerAdPosition);

         [System.Runtime.InteropServices.DllImport("__Internal")]
         extern static public void _isBannerAdShowing(string placement, int bannerAdSize);

         [System.Runtime.InteropServices.DllImport("__Internal")]
         extern static public void _closeBannerAd(string placement, int bannerAdSize);

#endif

#if UNITY_ANDROID
        private static AndroidJavaObject FreestarPlugin;

        private static AndroidJavaObject CreateAndroidPluginInstance()
        {
            using (var pluginClass = new AndroidJavaClass("com.freestar.android.unity.FreestarPlugin"))
            {
                FreestarPlugin = pluginClass.CallStatic<AndroidJavaObject>("GetInstance");
            }

            if (FreestarPlugin != null)
            {
                AndroidJavaClass javaClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
                AndroidJavaObject currentActivity = javaClass.GetStatic<AndroidJavaObject>("currentActivity");

                FreestarPlugin.Call("SetActivity", currentActivity);
                FreestarPlugin.Call("SetUnityAdListener", FreestarAndroidListener.GetInstance());
                FreestarAndroidListener.GetInstance().FreestarAdDelegateEventHandler += onFreestarEventReceiver;
            }
            else
            {
                Debug.Log("Unable to Initialize FreestarPlugin...");
            }
            return FreestarPlugin;
        }
        private static AndroidJavaObject AndroidPluginInstance()
        {
            return FreestarPlugin == null ? CreateAndroidPluginInstance() : FreestarPlugin;
        }
#endif

        private static FreestarInterstitialAdCallbackReceiver interRec;
        private static FreestarRewardedAdCallbackReceiver rewardRec;
        private static FreestarBannerAdCallbackReceiver bannerRec;

        public static void InitWithAPIKey(string apiKey)
        {
#if UNITY_IOS
            _initWithAPIKey(apiKey);
#endif

#if UNITY_ANDROID
            FreestarPlugin.Call("Init", apiKey);
#endif
        }

        /**
         * Only applies to Android/Google Play Store.  Call this method if Google has 
         * officially determined that your game must comply with the Google Families
         * policy.
         * 
         * mode: 0 = none   - Google Families policy does not apply to this game (default).
         *       1 = app    - This game is part of the Google Families program and is directed towards children.
         *       2 = mixed  - This game is part of the Google Families program and is directed towards children and families.
         *    
         * onlyNonPersonalizedAds: true - Only show non-personalized ads if the mode is 'mixed'.
         *                                If mode is not 'mixed', then this parameter will be ignored.
         *                         false - Will show personalized ads if the mode is 'mixed'.
         *                                If mode is not 'mixed', then this parameter will be ignored.
         */
        public static void SetGoogleFamiliesMode(int mode, bool onlyNonPersonalizedAds)
        {
#if UNITY_ANDROID
            FreestarPlugin.Call("SetGoogleFamiliesMode", mode, onlyNonPersonalizedAds);
#endif
        }

        private static FreestarIOSBannerMessagePasser bannerMiddleman;
        private static FreestarIOSInterstitialMessagePasser interstitialMiddleman;
        private static FreestarIOSRewardedMessagePasser rewardedMiddleman;


        public static void SetBannerAdListener(FreestarBannerAdCallbackReceiver listener)
        {
#if UNITY_IOS
            GameObject obj = new GameObject();
            bannerMiddleman = obj.AddComponent<FreestarIOSBannerMessagePasser>();
            bannerMiddleman.receiver = listener;
            Debug.Log("middleman object is " + bannerMiddleman.ToString());
            bannerMiddleman.name = "FSTRMiddleBanner" + listener.ToString();
            _setBannerListener(bannerMiddleman.name);
#endif

#if UNITY_ANDROID
            bannerRec = listener;
#endif

        }

        public static void SetInterstitialAdListener(FreestarInterstitialAdCallbackReceiver listener)
        {
#if UNITY_IOS
            GameObject obj = new GameObject();
            interstitialMiddleman = obj.AddComponent<FreestarIOSInterstitialMessagePasser>();
            interstitialMiddleman.receiver = listener;
            interstitialMiddleman.name = "FSTRMiddleInterstitial" + listener.ToString();
            _setInterstitialListener(interstitialMiddleman.name);
#endif

#if UNITY_ANDROID
            interRec = listener;
#endif

        }

        public static void SetRewardedAdListener(FreestarRewardedAdCallbackReceiver listener)
        {
#if UNITY_IOS
            GameObject obj = new GameObject();
            rewardedMiddleman = obj.AddComponent<FreestarIOSRewardedMessagePasser>();
            rewardedMiddleman.receiver = listener;
            rewardedMiddleman.name = "FSTRMiddleRewarded" + listener.ToString();
            _setRewardedListener(rewardedMiddleman.name);
#endif

#if UNITY_ANDROID
            rewardRec = listener;
#endif
        }

        public static void RemoveInterstitialAdListener()
        {
#if UNITY_ANDROID
            interRec = null;
#endif
#if UNITY_IOS
            _setInterstitialListener("");
#endif
        }

        public static void RemoveRewardedAdListener()
        {
#if UNITY_ANDROID
            rewardRec = null;
#endif
#if UNITY_IOS
            _setRewardedListener("");
#endif
        }

        public static void RemoveBannerAdListener()
        {
#if UNITY_ANDROID
            bannerRec = null;
#endif
#if UNITY_IOS
            _setBannerListener("");
#endif
        }

        public static void LoadInterstitialAd(string placement)
        {
            placement = placement + "";
#if UNITY_IOS
    			_loadInterstitialAd(placement);
#endif

#if UNITY_ANDROID
            FreestarPlugin.Call("LoadInterstitialAd", placement);
#endif
        }

        public static void ShowInterstitialAd(string placement)
        {
#if UNITY_IOS
    		   _showInterstitialAd(placement);
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("ShowInterstitialAd", placement);
            }
#endif
        }

        public static void LoadRewardedAd(string placement)
        {
            placement = placement + "";
#if UNITY_IOS
            _loadRewardedAd(placement);
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("LoadRewardedAd", placement);
            }
#endif
        }

        public static void ShowRewardedAd(string placement, int rewardAmount, string rewardName, string userId, string secretKey)
        {
            placement = placement + "";
#if UNITY_IOS
            _showRewardedAd(placement, rewardAmount, rewardName, userId, secretKey);
#endif

#if UNITY_ANDROID

            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("ShowRewardedAd", placement, secretKey, userId, rewardName, "" + rewardAmount);
            }
#endif
        }

        public static void SetAdRequestUserParams(int age, string birthDate, string gender, string maritalStatus, string ethnicity,
                                                      string dmaCode, string postal, string curPostal, string latitude, string longitude)
        {

#if UNITY_IOS
            _setDemograpics(age, birthDate, gender, maritalStatus, ethnicity);
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("SetAdRequestUserParams",
                    "" + age,
                    birthDate,
                    gender,
                    maritalStatus,
                    ethnicity,
                    dmaCode,
                    postal, curPostal, latitude, longitude);
            }
#endif
        }

        public static void SetDemograpics(int age, string birthDate, string gender, string maritalStatus,
                             string ethnicity)
        {
#if UNITY_IOS
            _setDemograpics(age,birthDate,gender,maritalStatus,ethnicity);
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("SetAdRequestUserParams",
                    "" + age,
                    birthDate,
                    gender,
                    maritalStatus,
                    ethnicity, "", "", "", "", "");
            }
#endif
        }

        public static void SetLocation(string dmaCode, string postal, string curPostal, string latitude, string longitude)
        {

#if UNITY_IOS
            _setLocation(dmaCode, postal, curPostal, latitude, longitude);
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("SetAdRequestUserParams",
                    "", "", "", "", "",
                    dmaCode,
                    postal,
                    curPostal,
                    latitude,
                    longitude);
            }
#endif
        }

        public static void SetCustomSegmentProperty(string key, string value)
        {
#if UNITY_IOS
            _setCustomSegmentProperty(key,value);
#endif

#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("SetCustomSegmentProperty", key, value);
            }
            catch (Exception e)
            {
                Debug.Log("setCustomSegmentProperty failed: " + e);
            }
#endif

        }

        public static string GetCustomSegmentProperty(string key)
        {
#if UNITY_IOS
            return _getCustomSegmentProperty(key);
#endif

#if UNITY_ANDROID
            try
            {
                return FreestarPlugin.Call<string>("GetCustomSegmentProperty", key);
            }
            catch (Exception e)
            {
                Debug.Log("getCustomSegmentProperty failed: " + e);                
            }
#endif
            return null;
        }

        public static Dictionary<string, string> GetAllCustomSegmentProperties()
        {
#if UNITY_IOS

            string jsonRep = _getAllCustomSegmentProperties();
            return JsonUtility.FromJson<Dictionary<string, string>>(jsonRep);
#endif

#if UNITY_ANDROID
            try
            {
                string jsonRep = FreestarPlugin.Call<string>("GetAllCustomSegmentProperties");
                if (jsonRep != null)
                {
                    return JsonUtility.FromJson<Dictionary<string, string>>(jsonRep);
                }
                else
                {
                    return null;
                }
            }
            catch (Exception e)
            {
                Debug.Log("getAllCustomSegmentProperties failed: " + e);                
            }
#endif
            return null;
        }

        public static void DeleteCustomSegmentProperty(string key)
        {
#if UNITY_IOS
            _deleteCustomSegmentProperty(key);
#endif

#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("DeleteCustomSegmentProperty", key);
            }
            catch (Exception e)
            {
                Debug.Log("deleteCustomSegmentProperty failed: " + e);
            }
#endif
        }

        public static void DeleteAllCustomSegmentProperties()
        {
#if UNITY_IOS
            _deleteAllCustomSegmentProperties();
#endif

#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("DeleteAllCustomSegmentProperties");
            }
            catch (Exception e)
            {
                Debug.Log("deleteAllCustomSegmentProperties() failed: " + e);
            }
#endif
        }

        public static void SetAdRequestTestMode(bool isTestMode)
        {
#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("SetTestModeEnabled", isTestMode);
            }
#endif

#if UNITY_IOS
            Debug.Log("SetAdRequestTestMode api not available on iOS yet");
            //not supported by iOS yet
#endif

        }

        //This method calls Native Method to Check Reward Ad Availability
        //Returns true if Available and ready else return false
        public static bool IsRewardedAdAvailableToShow(string placement)
        {
            Debug.Log("FreestarUnityBridge Check Reward Ad...");
            bool isAvailable = false;
            placement = placement + "";

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                isAvailable = FreestarPlugin.Call<bool>("IsRewardedAdAvailableToShow", placement);
            }
            Debug.Log("Is Rewarded Ad available: " + isAvailable);
#endif

#if UNITY_IOS
            Debug.Log("IsRewardedAdAvailableToShow api not available on iOS yet");
#endif
            return isAvailable;
        }

        public static bool IsInterstitialAdAvailableToShow(string placement)
        {
            placement = placement + "";
            Debug.Log("FreestarUnityBridge Check Interstitial Ad...");
            bool isAvailable = false;

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                isAvailable = FreestarPlugin.Call<bool>("IsInterstitialAdAvailableToShow", placement);
            }
            Debug.Log("Is Interstitial Ad available: " + isAvailable);
#endif

#if UNITY_IOS
            Debug.Log("IsInterstitialAdAvailableToShow api not available on iOS yet");   //TODO
#endif

            return isAvailable;
        }

        //Invoke with true if you want the possibility of some banner ads to be fullscreen width.
        //By default, this will be false.
        public static void ShowAdaptiveBannerAdsWhenAvailable(bool showAdaptiveBannerAdsWhenAvailable)
        {

        #if UNITY_ANDROID
                 if (AndroidPluginInstance() != null)
                 {
                     FreestarPlugin.Call("ShowAdaptiveBannerAdsWhenAvailable", showAdaptiveBannerAdsWhenAvailable);
                 }
                 Debug.Log("ShowAdaptiveBannerAdsWhenAvailable: " + showAdaptiveBannerAdsWhenAvailable);
        #endif

        #if UNITY_IOS
                 Debug.Log("ShowAdaptiveBannerAdsWhenAvailable api not available on iOS yet");   //TODO
        #endif

        }

       /**
        *  Only use for automated testing of your game/app and you want to skip or limit
        *  ads when testing.
        *
        *  Do NOT use for production builds!
        *
        *  AutomatedTestMode: 0 = bypass all ads
        *                     1 = limited mediation
        */
        public static void SetAutomatedTestMode(int automatedTestMode)
        {

        #if UNITY_ANDROID
                 if (AndroidPluginInstance() != null)
                 {
                     FreestarPlugin.Call("SetAutomatedTestMode", automatedTestMode);
                 }
                 Debug.Log("SetAutomatedTestMode: " + automatedTestMode);
        #endif

        #if UNITY_IOS
                 Debug.Log("SetAutomatedTestMode api not available on iOS yet");   //TODO
        #endif

        }

        //OPTIONAL! Set unique user id of your application, if you wish.
        public static void SetUserId(string userId)
        {
            Debug.Log("SetUserId: " + userId);
#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("SetUserId", userId);
            }
#endif

#if UNITY_IOS
            Debug.Log("SetUserId api not available on iOS yet");
#endif
        }

        public static void ShowPartnerChooser(bool showPartnerChooser) //TODO iOS
        {
#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("ShowPartnerChooser", showPartnerChooser);
            }
            catch (Exception e)
            {
                Debug.Log("ShowPartnerChooser failed: " + e);
            }
#endif

#if UNITY_IOS
            Debug.Log("ShowPartnerChooser api not available on iOS yet");
#endif
        }

        public static string GetRewardedAdWinner(string placement) //TODO iOS
        {
            placement = placement + "";
#if UNITY_ANDROID
            try
            {
                return FreestarPlugin.Call<string>("GetRewardedAdWinner", placement);
            }
            catch (Exception e)
            {
                Debug.Log("GetRewardedAdWinner failed: " + e);
            }
#endif

#if UNITY_IOS
            Debug.Log("GetRewardedAdWinner api not available on iOS yet");
#endif
            return "";
        }

        public static string GetInterstitialAdWinner(string placement)
        {
            placement = placement + "";
#if UNITY_ANDROID
            try
            {
                return FreestarPlugin.Call<string>("GetInterstitialAdWinner", placement);
            }
            catch (Exception e)
            {
                Debug.Log("GetInterstitialAdWinner failed: " + e);
            }
#endif

#if UNITY_IOS
            Debug.Log("GetInterstitialAdWinner api not available on iOS yet");
#endif
            return "";
        }

#if UNITY_ANDROID

        private static void onFreestarEventReceiver(string placement, string adType, int adSize, string eventName)
        {
            Debug.Log("Ad Event Received: " + eventName
            + "  AdType: " + adType
            + "  AdSize: " + adSize
            + "  Placement: [" + placement + "]");
            if (eventName == FreestarConstants.INTERSTITIAL_AD_LOADED)
            {
                interRec.onInterstitialAdLoaded(placement);
            }
            else if (eventName == FreestarConstants.INTERSTITIAL_AD_FAILED)
            {
                interRec.onInterstitialAdFailed(placement);
            }
            else if (eventName == FreestarConstants.INTERSTITIAL_AD_SHOWN)
            {
                interRec.onInterstitialAdShown(placement);
            }
            else if (eventName == FreestarConstants.INTERSTITIAL_AD_DISMISSED)
            {
                interRec.onInterstitialAdDismissed(placement);
            }
            else if (eventName == FreestarConstants.INTERSTITIAL_AD_CLICKED)
            {
                interRec.onInterstitialAdClicked(placement);
            }
            else if (eventName == FreestarConstants.REWARDED_AD_LOADED)
            {
                rewardRec.onRewardedAdLoaded(placement);
            }
            else if (eventName == FreestarConstants.REWARDED_AD_FAILED)
            {
                rewardRec.onRewardedAdFailed(placement);
            }
            else if (eventName == FreestarConstants.REWARDED_AD_SHOWN)
            {
                rewardRec.onRewardedAdShown(placement);
            }
            else if (eventName == FreestarConstants.REWARDED_AD_SHOWN_ERROR)
            {
                rewardRec.onRewardedAdFailed(placement);
            }
            else if (eventName == FreestarConstants.REWARDED_AD_DISMISSED)
            {
                rewardRec.onRewardedAdDismissed(placement);
            }
            else if (eventName == FreestarConstants.REWARDED_AD_COMPLETED)
            {
                rewardRec.onRewardedAdFinished(placement);
                //If you setup server-to-server (S2S) rewarded callbacks you can
                //assume your server url will get hit at this time.
                //Or you may choose to reward your user from the client here.

            }
            else if (eventName == FreestarConstants.BANNER_AD_SHOWING)
            {
                bannerRec.onBannerAdShowing(placement, adSize);
            }
            else if (eventName == FreestarConstants.BANNER_AD_FAILED)
            {
                bannerRec.onBannerAdFailed(placement, adSize);
            }
            else if (eventName == FreestarConstants.BANNER_AD_CLICKED)
            {
                bannerRec.onBannerAdClicked(placement, adSize);
            }

        }

#endif

        /**
         * Placement:           ad unit placement (can pass in null if you don't have one)
         * 
         * bannerAdSize:        Choose from:
         *                      FreestarConstants.BANNER_AD_SIZE_320x50 
         *                      FreestarConstants.BANNER_AD_SIZE_300x250
         *                      FreestarConstants.BANNER_AD_SIZE_728x90
         *                   
         * bannerAdPosition:    Choose from:
         *                      FreestarConstants.BANNER_AD_POSITION_BOTTOM
         *                      FreestarConstants.BANNER_AD_POSITION_MIDDLE
         *                      FreestarConstants.BANNER_AD_POSITION_TOP
         */
        public static void ShowBannerAd(string placement, int bannerAdSize, int bannerAdPosition)
        {
            placement = placement + "";
#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("ShowBannerAd", placement, bannerAdSize, bannerAdPosition);
            }
            catch (Exception e)
            {
                Debug.Log("ShowBannerAd failed: " + e);
            }
#endif

#if UNITY_IOS
            _showBannerAd(placement, bannerAdSize, bannerAdPosition);
#endif
        }

        /**
         * Placement:           ad unit placement (can pass in null if you don't have one)
         * 
         * bannerAdSize:        Choose from:
         *                      FreestarConstants.BANNER_AD_SIZE_320x50 
         *                      FreestarConstants.BANNER_AD_SIZE_300x250 
         *
         */
        public static bool IsBannerAdShowing(string placement, int bannerAdSize)
        {
            placement = placement + "";
#if UNITY_ANDROID
            try
            {
                return FreestarPlugin.Call<bool>("IsBannerAdShowing", placement, bannerAdSize);
            }
            catch (Exception e)
            {
                Debug.Log("ShowBannerAd failed: " + e);
                return false;
            }
#endif

#if UNITY_IOS
            _isBannerAdShowing(placement, bannerAdSize);
            
#endif
            return false;
        }

        public static string GetBannerAdWinner(string placement, int bannerAdSize)
        {
            placement = placement + "";
#if UNITY_ANDROID
            try
            {
                return FreestarPlugin.Call<string>("GetBannerAdWinner", placement, bannerAdSize);
            }
            catch (Exception e)
            {
                Debug.Log("GetBannerAdWinner failed: " + e);
                return "";
            }
#endif

#if UNITY_IOS
            Debug.Log("GetBannerAdWinner api not available on iOS yet");
#endif
            return "";
        }

        /**
         * Placement:           ad unit placement (can pass in null if you don't have one)
         * 
         * bannerAdSize:        Choose from:
         *                      FreestarConstants.BANNER_AD_SIZE_320x50 
         *                      FreestarConstants.BANNER_AD_SIZE_300x250 
         *
         */
        public static void CloseBannerAd(string placement, int bannerAdSize)
        {
            placement = placement + "";
#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("CloseBannerAd", placement, bannerAdSize);
            }
            catch (Exception e)
            {
                Debug.Log("CloseBannerAd failed: " + e);
            }
#endif

#if UNITY_IOS
            _closeBannerAd(placement, bannerAdSize);
#endif
        }

        public static void Pause()
        {
#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("OnPause");
            }
            catch (Exception e)
            {
                Debug.Log("FreestarPlugin OnPause failed: " + e);
            }
#endif
        }

        public static void Resume() 
        {
#if UNITY_ANDROID
            try
            {
                FreestarPlugin.Call("OnResume");
            }
            catch (Exception e)
            {
                Debug.Log("FreestarPlugin OnResume failed: " + e);
            }
#endif
        }

    }

}
