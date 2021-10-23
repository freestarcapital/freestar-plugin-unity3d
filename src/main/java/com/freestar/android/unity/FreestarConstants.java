package com.freestar.android.unity;

class FreestarConstants {

    //AD UNIT TYPES
    static final String INTERSTITIAL_AD_TYPE = "FULLSCREEN_INTERSTITIAL";
    static final String REWARDED_AD_TYPE = "FULLSCREEN_REWARDED";
    static final String BANNER_AD_TYPE = "BANNER";

    //AD SIZES
    static final int BANNER_AD_SIZE_320x50 = 0;
    static final int BANNER_AD_SIZE_300x250 = 1;
    static final int BANNER_AD_SIZE_728x90 = 2;
    static final int FULLSCREEN_AD_SIZE = 3;

    static final int BANNER_AD_POSITION_BOTTOM = 0;
    static final int BANNER_AD_POSITION_MIDDLE = 1;
    static final int BANNER_AD_POSITION_TOP = 2;

    //EVENTS
    static final String FREESTAR_SUCCESSFULLY_INITIALIZED = "FREESTAR_SUCCESSFULLY_INITIALIZED";
    static final String FREESTAR_FAILED_TO_INITIALIZE = "FREESTAR_FAILED_TO_INITIALIZE";

    static final String INTERSTITIAL_AD_LOADED = "INTERSTITIAL_AD_LOADED";
    static final String INTERSTITIAL_AD_FAILED = "INTERSTITIAL_AD_FAILED";
    static final String INTERSTITIAL_AD_SHOWN = "INTERSTITIAL_AD_SHOWN";
    static final String INTERSTITIAL_AD_DISMISSED = "INTERSTITIAL_AD_DISMISSED";
    static final String INTERSTITIAL_AD_CLICKED = "INTERSTITIAL_AD_CLICKED";

    static final String REWARDED_AD_LOADED = "REWARDED_AD_LOADED";
    static final String REWARDED_AD_FAILED = "REWARDED_AD_FAILED";
    static final String REWARDED_AD_SHOWN = "REWARDED_AD_SHOWN";
    static final String REWARDED_AD_SHOWN_ERROR = "REWARDED_AD_SHOWN_ERROR";
    static final String REWARDED_AD_DISMISSED = "REWARDED_AD_DISMISSED";
    static final String REWARDED_AD_COMPLETED = "REWARDED_AD_COMPLETED";

    static final String BANNER_AD_SHOWING = "BANNER_AD_SHOWING";
    static final String BANNER_AD_FAILED = "BANNER_AD_FAILED";
    static final String BANNER_AD_CLICKED = "BANNER_AD_CLICKED";
}
