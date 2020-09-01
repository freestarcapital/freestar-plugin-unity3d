﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Runtime.InteropServices;
using System;

namespace Freestar
{

    public interface FreestarInterstitialCallbackReceiver
    {
        void onInterstitialLoaded(string msg);
        void onInterstitialFailed(string msg);
        void onInterstitialShown(string msg);
        void onInterstitialClicked(string msg);
        void onInterstitialDismissed(string msg);
    }

    public interface FreestarRewardedCallbackReceiver
    {
        void onRewardLoaded(string msg);
        void onRewardFailed(string msg);
        void onRewardShown(string msg);
        void onRewardFinished(string msg);
        void onRewardDismissed(string msg);
    }


    public class FreestarUnityBridge : MonoBehaviour
    {

#if UNITY_IOS

         [System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _initWithAPIKey(string apiKey);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setupWithListener(string listenerName);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _loadInterstitialAd();

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _showInterstitialAd();

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _loadRewardAd();

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _showRewardAd(int rewardAmount, string rewardName, string userId, string secretKey);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setDemograpics(int age, string birthDate, string gender, string maritalStatus,
    		                     string ethnicity);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setLocation(string dmaCode, string postal, string curPostal, string latitude, string longitude);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setAppInfo(string appName, string pubName,
    		                 string appDomain, string pubDomain,
    		                 string storeUrl, string iabCategory);

    		[System.Runtime.InteropServices.DllImport("__Internal")]
    		extern static public void _setPrivacySettings(bool gdprApplies, string gdprConsentString);

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

        //private static GameObject callbackListener;
        private static FreestarInterstitialCallbackReceiver interRec;
        private static FreestarRewardCallbackReceiver rewardRec;

        //invokes the Objective-C fuctions only on iOS, not in Unity
        public static void initWithAPIKey(string apiKey)
        {
#if UNITY_IOS
                _initWithAPIKey(apiKey);
#endif

#if UNITY_ANDROID
               FreestarPlugin.Call("Init", apiKey);
#endif
        }

        public static void setInterstitialAdListener(FreestarInterstitialCallbackReceiver listener)
        {
#if UNITY_IOS
                _setupWithListener(listener.ToString());
#endif

#if UNITY_ANDROID
            interRec = listener;
#endif

        }

        public static void setRewardAdListener(FreestarRewardedCallbackReceiver listener)
        {
#if UNITY_IOS
                _setupWithListener(listener.ToString());
#endif

#if UNITY_ANDROID
            rewardRec = listener;
#endif
        }

        public static void removeInterstitialAdListener()
        {
#if UNITY_ANDROID
            interRec = null;
#endif
        }

        public static void removeRewardAdListener()
        {
#if UNITY_ANDROID
            rewardRec = null;
#endif
        }

        public static void loadInterstitialAd(string placement)
        {
#if UNITY_IOS
    			_loadInterstitialAd(placement);
#endif

#if UNITY_ANDROID
            FreestarPlugin.Call("LoadInterstitialAd", placement);
        }
#endif

        public static void showInterstitialAd()
        {
#if UNITY_IOS
    		_showInterstitialAd();
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("ShowInterstitialAd", placement);
            }
#endif
        }

        public static void loadRewardAd()
        {
#if UNITY_IOS
                _loadRewardAd();
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("LoadRewardAd", placement);
            }
        }
#endif

        public static void showRewardAd(int rewardAmount, string rewardName, string userId, string secretKey)
        {

#if UNITY_IOS
            _showRewardAd(rewardAmount, rewardName, userId, secretKey);
#endif

#if UNITY_ANDROID

            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("ShowRewardAd", secretKey, userId, rewardName, "" + rewardAmount);
            }
        }
#endif    


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

        public static void setDemograpics(int age, string birthDate, string gender, string maritalStatus,
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

        public static void setLocation(string dmaCode, string postal, string curPostal, string latitude, string longitude)
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

        public static void setAppInfo(string appName, string pubName,
                         string appDomain, string pubDomain,
                         string storeUrl, string iabCategory)
        {

#if UNITY_IOS
                _setAppInfo(appName,pubName,appDomain,pubDomain,storeUrl,iabCategory);
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("SetAdRequestAppParams",
                    appName,
                    pubName,
                    appDomain,
                    pubDomain,
                    storeUrl,
                    iabCategory);
            }
#endif
        }

        public static void setPrivacySettings(bool gdprApplies, string gdprConsentString)
        {
#if UNITY_IOS
    				_setPrivacySettings(gdprApplies,gdprConsentString);
#endif

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                try
                {
                    FreestarPlugin.Call("SetPrivacySettings", gdprApplies, gdprConsentString);
                }
                catch (Exception e)
                {
                    Debug.Log("setPrivacySettings failed: " + e);
                }
            }
#endif
        }

        public static void setCustomSegmentProperty(string key, string value)
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

        public static string getCustomSegmentProperty(string key)
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
                return null;
            }
#endif
        }

        public static Dictionary<string, string> getAllCustomSegmentProperties()
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
                return null;
            }
#endif
        }

        public static void deleteCustomSegmentProperty(string key)
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

        public static void deleteAllCustomSegmentProperties()
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

        public static void SetAdRequestTestMode(bool isTestMode, string testID)
        {
#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                FreestarPlugin.Call("SetTestModeEnabled", isTestMode, testID);
            }
#endif

#if UNITY_IOS
            Debug.Log("SetAdRequestTestMode api not available on iOS yet");
            //not supported by iOS yet
#endif

        }

        //This method calls Native Method to Check Reward Ad Availability
        //Returns true if Available and ready else return false
        public static bool IsRewardAdAvailableToShow()
        {
            Debug.Log("FreestarUnityBridge Check Reward Ad...");
            bool isAvailable = false;

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                isAvailable = FreestarPlugin.Call<bool>("IsRewardAdAvailableToShow");
            }
            Debug.Log("Is Reward Ad available: " + isAvailable);
#endif

#if UNITY_IOS
            Debug.Log("IsRewardAdAvailableToShow api not available on iOS yet");
#endif
            return isAvailable;
        }

        public static bool IsInterstitialAdAvailableToShow()
        {
            Debug.Log("FreestarUnityBridge Check Interstitial Ad...");
            bool isAvailable = false;

#if UNITY_ANDROID
            if (AndroidPluginInstance() != null)
            {
                isAvailable = FreestarPlugin.Call<bool>("IsInterstitialAdAvailableToShow");
            }
            Debug.Log("Is Interstitial Ad available: " + isAvailable);
#endif

#if UNITY_IOS
            Debug.Log("IsInterstitialAdAvailableToShow api not available on iOS yet");
#endif

            return isAvailable;
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

        public static string GetRewardAdWinner()
        {
#if UNITY_ANDROID
            try
            {
                return FreestarPlugin.Call<string>("GetRewardAdWinner");
            }
            catch (Exception e)
            {
                Debug.Log("GetRewardAdWinner failed: " + e);
                return "";
            }
#endif

#if UNITY_IOS
            Debug.Log("GetRewardAdWinner api not available on iOS yet");
            return "";
#endif
        }

        public static string GetInterstitialAdWinner()
        {
#if UNITY_ANDROID
            try
            {
                return FreestarPlugin.Call<string>("GetInterstitialAdWinner");
            }
            catch (Exception e)
            {
                Debug.Log("GetInterstitialAdWinner failed: " + e);
                return "";
            }
#endif

#if UNITY_IOS
            Debug.Log("GetInterstitialAdWinner api not available on iOS yet");
            return "";
#endif
        }

#if UNITY_ANDROID

        private static void onFreestarEventReceiver(string adType, string eventName)
        {
            Debug.Log("Ad Event Received : " + eventName + " : For Ad Type : " + adType);
            if ((eventName.Contains("INTERSTITIAL") && interRec == null) ||
               (eventName.Contains("REWARD") && rewardRec == null))
            {
                Debug.Log("No callback listener detected");
                return;
            }

            if (eventName == "INTERSTITIAL_LOADED")
            {
                interRec.onInterstitialLoaded("");
            }
            else if (eventName == "INTERSTITIAL_FAILED")
            {
                interRec.onInterstitialFailed("");
            }
            else if (eventName == "INTERSTITIAL_SHOWN")
            {
                interRec.onInterstitialShown("");
            }
            else if (eventName == "INTERSTITIAL_DISMISSED")
            {
                interRec.onInterstitialDismissed("");
            }
            else if (eventName == "INTERSTITIAL_CLICKED")
            {
                interRec.onInterstitialClicked("");
            }
            else if (eventName == "REWARD_AD_LOADED")
            {
                rewardRec.onRewardLoaded("");
            }
            else if (eventName == "REWARD_AD_FAILED")
            {
                rewardRec.onRewardFailed("");
            }
            else if (eventName == "REWARD_AD_SHOWN")
            {
                rewardRec.onRewardShown("");
            }
            else if (eventName == "REWARD_AD_SHOWN_ERROR")
            {
                rewardRec.onRewardFailed("");
            }
            else if (eventName == "REWARD_AD_DISMISSED")
            {
                rewardRec.onRewardDismissed("");
            }
            else if (eventName == "REWARD_AD_COMPLETED")
            {
                rewardRec.onRewardFinished("");
                //If you setup server-to-server (S2S) rewarded callbacks you can
                //assume your server url will get hit at this time.
                //Or you may choose to reward your user from the client here.

            }
        }
#endif
    }
}
