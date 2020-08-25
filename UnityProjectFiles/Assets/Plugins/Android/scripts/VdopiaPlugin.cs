using UnityEngine;
using System;

namespace Vdopia
{
    public class VdopiaPlugin
    {
        public static readonly string CHOCOLATE_SUCCESSFULLY_INITIALIZED = "CHOCOLATE_SUCCESSFULLY_INITIALIZED";
        public static readonly string CHOCOLATE_FAILED_TO_INITIALIZE = "CHOCOLATE_FAILED_TO_INITIALIZE";

        // AD Types Interstitial and Reward Used to identify adtype in callback
        public static readonly string INTERSTITIAL_AD_TYPE = "INTERSTITIAL";
        public static readonly string REWARD_AD_TYPE = "REWARD";

        // AD Event Message For Interstitial and Reward Used to identify event in callback
        public static readonly string INTERSTITIAL_AD_LOADED = "INTERSTITIAL_LOADED";
        public static readonly string INTERSTITIAL_AD_FAILED = "INTERSTITIAL_FAILED";
        public static readonly string INTERSTITIAL_AD_SHOWN = "INTERSTITIAL_SHOWN";
        public static readonly string INTERSTITIAL_AD_DISMISSED = "INTERSTITIAL_DISMISSED";
        public static readonly string INTERSTITIAL_AD_CLICKED = "INTERSTITIAL_CLICKED";

        public static readonly string REWARD_AD_LOADED = "REWARD_AD_LOADED";
        public static readonly string REWARD_AD_FAILED = "REWARD_AD_FAILED";
        public static readonly string REWARD_AD_SHOWN = "REWARD_AD_SHOWN";
        public static readonly string REWARD_AD_SHOWN_ERROR = "REWARD_AD_SHOWN_ERROR";
        public static readonly string REWARD_AD_DISMISSED = "REWARD_AD_DISMISSED";
        public static readonly string REWARD_AD_COMPLETED = "REWARD_AD_COMPLETED";

        //Native Plugin Instance to call Native Method
        private AndroidJavaObject VDONativePlugin;

        //Singleton Plugin Instance to call method of this class
        private static VdopiaPlugin instance;

        public static VdopiaPlugin GetInstance()
        {
            if (instance == null)
            {
                instance = new VdopiaPlugin();
            }

            return instance;
        }

        private VdopiaPlugin()
        {
            if (Application.platform == RuntimePlatform.Android)
            {
                //Initialize VdopiaPlugin
                if (VDONativePlugin == null)
                {
                    using (var pluginClass = new AndroidJavaClass("com.vdopia.unity.plugin.VdopiaPlugin"))
                    {
                        VDONativePlugin = pluginClass.CallStatic<AndroidJavaObject>("GetInstance");
                    }
                }

                //Setting Context and Listener to Plugin
                if (VDONativePlugin != null)
                {
                    AndroidJavaClass javaClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
                    AndroidJavaObject currentActivity = javaClass.GetStatic<AndroidJavaObject>("currentActivity");

                    VDONativePlugin.Call("SetActivity", currentActivity);
                    VDONativePlugin.Call("SetUnityAdListener", VdopiaListener.GetInstance());
                }
                else
                {
                    Debug.Log("Unable to Initialize VdopiaPlugin...");
                }
            }
        }

        //This method calls Native Method to Set Targeting Params related to the User
        public void SetAdRequestUserData(String age, String birthDate, String gender, String marital,
                                           String ethnicity, String dmaCode, String postal, String curPostal,
											String lat, String lon)
        {
            Debug.Log("SetAdRequestUserData...");
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
				VDONativePlugin.Call("SetAdRequestUserParams", age, birthDate, gender, marital,
					ethnicity, dmaCode, postal, curPostal, lat, lon);
            }
        }

        //This method calls Native Method to Set Targeting Params related to the App
        public void SetAdRequestAppData(String appName, String pubName,
                                          String appDomain, String pubDomain,
                                          String storeUrl, String iabCategory)
        {
            Debug.Log("SetAdRequestAppData...");
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
                VDONativePlugin.Call("SetAdRequestAppParams", appName, pubName, appDomain, pubDomain,
                                                            storeUrl, iabCategory);
            }
        }

        //This method calls Native Method to Set Test Mode (For Test Ad of Facebook/Google Partner)
        public void SetAdRequestTestMode(bool isTestMode, String testID)
        {
            Debug.Log("SetAdRequestTestMode...");
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
                VDONativePlugin.Call("SetTestModeEnabled", isTestMode, testID);
            }
        }

        //Initialize Chocolate Platform SDK!
        public void ChocolateInit(String apiKey)
        {
           Debug.Log("ChocolateInit...");
           if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
           {
               VDONativePlugin.Call("ChocolateInit", apiKey);
           }
        }

        //Prefetches interstitial ad in background and caches it.  There are NO callbacks for this.
        public void PrefetchInterstitialAd(String apiKey)
        {
           Debug.Log("Prefetch Interstitial...");
           if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
           {
               //VDONativePlugin.Call("PrefetchInterstitialAd", apiKey);
               //pre-fetching is now done internally via the chocolate sdk
           }
        }

        //This method calls Native Method to Load Interstitial Ad
        public void LoadInterstitialAd(String apiKey)
        {
            Debug.Log("VdopiaPlugin: Load Interstitial...");
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
                VDONativePlugin.Call("LoadInterstitialAd", apiKey);
            }
        }

        //This method calls Native Method to Show Interstitial Ad
        public void ShowInterstitialAd()
        {
            Debug.Log("VdopiaPlugin Show Interstitial...");
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
                VDONativePlugin.Call("ShowInterstitialAd");
            }
        }

        //Prefetches rewarded ad in background and caches it.  There are NO callbacks for this.
        public void PrefetchRewardAd(String apiKey)
        {
           Debug.Log("Prefetch Reward...");
           if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
           {
                //VDONativePlugin.Call("PrefetchRewardAd", apiKey);
                //pre-fetching is now done internally via the chocolate sdk
            }
        }

        //This method calls Native Method to Load Reward Ad
        public void RequestRewardAd(String apiKey)
        {
            Debug.Log("VdopiaPlugin Request Reward...");
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
                VDONativePlugin.Call("LoadRewardAd", apiKey);
            }
        }

        //This method calls Native Method to Show Reward Ad
        public void ShowRewardAd(String secret, String userId, String rewardName, String rewardAmount)
        {
            Debug.Log("VdopiaPlugin Show Reward...");
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
                VDONativePlugin.Call("ShowRewardAd", secret, userId, rewardName, rewardAmount);
            }
        }

        //This method calls Native Method to Check Reward Ad Availability
        //Returns true if Available and ready else return false
        public bool IsRewardAdAvailableToShow()
        {
            Debug.Log("VdopiaPluginCheck Reward...");
            bool isAvailable = false;
            if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
            {
                isAvailable = VDONativePlugin.Call<bool>("IsRewardAdAvailableToShow");
            }

            Debug.Log("Is Reward Ad available: " + isAvailable);
            return isAvailable;
        }

       public bool IsInterstitialAdAvailableToShow()
       {
           Debug.Log("VdopiaPluginCheck Interstitial...");
           bool isAvailable = false;
           if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
           {
               isAvailable = VDONativePlugin.Call<bool>("IsInterstitialAdAvailableToShow");
           }

           Debug.Log("Is Interstitial Ad available: " + isAvailable);
           return isAvailable;
       }

        //OPTIONAL! Set unique user id of your application, if you wish.
        public void SetUserId(String userId)
        {
           Debug.Log("SetUserId: " + userId);
           if (Application.platform == RuntimePlatform.Android && VDONativePlugin != null)
           {
               VDONativePlugin.Call("SetUserId", userId);
           }
        }

        public string GetRewardAdWinner()
        {
            try
            {
                return VDONativePlugin.Call<string>("GetRewardAdWinner");
            } catch (Exception e)
            {
                Debug.Log("GetRewardAdWinner failed: " + e);
                return "";
            }
        }

        public string GetInterstitialAdWinner()
        {
            try
            {
                return VDONativePlugin.Call<string>("GetInterstitialAdWinner");
            }catch(Exception e)
            {
                Debug.Log("GetInterstitialAdWinner failed: " + e);
                return "";
            }
        }
    }
}
