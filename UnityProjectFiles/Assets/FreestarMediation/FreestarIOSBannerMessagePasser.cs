using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Runtime.InteropServices;
using System;

namespace Freestar
{
    
    public class FreestarIOSBannerMessagePasser : MonoBehaviour {
        public FreestarBannerAdCallbackReceiver receiver;

        public void BannerAdShown(string data) {
            string[] elems = data.Split(',');
            this.receiver.onBannerAdShowing(elems[0], int.Parse(elems[1]));
        }

        public void BannerAdClicked(string data) {
            string[] elems = data.Split(',');
            this.receiver.onBannerAdClicked(elems[0], int.Parse(elems[1]));
        }

        public void BannerAdFailed(string data) {
            string[] elems = data.Split(',');
            this.receiver.onBannerAdFailed(elems[0], int.Parse(elems[1]));
        }
    }

    public class FreestarIOSInterstitialMessagePasser : MonoBehaviour, FreestarInterstitialAdCallbackReceiver {
        public FreestarInterstitialAdCallbackReceiver receiver;

        public void onInterstitialAdLoaded(string placement) {
            this.receiver.onInterstitialAdLoaded(placement);
        }
        public void onInterstitialAdFailed(string placement) {
            this.receiver.onInterstitialAdFailed(placement);
        }
        public void onInterstitialAdShown(string placement) {
            this.receiver.onInterstitialAdShown(placement);
        }
        public void onInterstitialAdClicked(string placement) {
            this.receiver.onInterstitialAdClicked(placement);
        }
        public void onInterstitialAdDismissed(string placement) {
            this.receiver.onInterstitialAdDismissed(placement);
        }
    }

    public class FreestarIOSRewardedMessagePasser : MonoBehaviour, FreestarRewardedAdCallbackReceiver {
        public FreestarRewardedAdCallbackReceiver receiver;

        public void onRewardedAdLoaded(string placement) {
            this.receiver.onRewardedAdLoaded(placement);
        }
        public void onRewardedAdFailed(string placement) {
            this.receiver.onRewardedAdFailed(placement);
        }
        public void onRewardedAdShown(string placement) {
            this.receiver.onRewardedAdShown(placement);
        }
        public void onRewardedAdFinished(string placement) {
            this.receiver.onRewardedAdFinished(placement);
        }
        public void onRewardedAdDismissed(string placement) {
            this.receiver.onRewardedAdDismissed(placement);
        }
    }

    
    
}