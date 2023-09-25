package com.amri.emploihunt.util

// 110907
// Testing OTP: 066827
// Testing No's: 79903 80970 & 84602 05619  & ishita mam: +971585421911 - 123456
const val DEVICE_ID = "vDeviceId"
const val OS_VERSION = "vOSVersion"
const val FCM_TOKEN = "tDeviceToken"
const val AUTH_TOKEN = "tAuthToken"
//const val GOOGLE_ID = "googleid"
//const val FB_ID = "fbid"
const val IS_SKIP = "is_skip"
const val IS_LOGIN = "is_login" // manage IS_SKIP function with Login... if LOGIN then SKIPPED == false else SKIPPED == true
const val FIREBASE_ID = "vFirebaseId"
const val DEVICE_NAME = "tDeviceName"
const val LATITUDE = "latitude"
const val LONGITUDE = "longitude"
const val DEVICE_TYPE = "vDeviceType" // 0 - Android
// User Details
const val USER_ID = "user_id"
const val MOB_NO = "vMobile"
const val REFERRAL_CODE = "referral_code"
const val COUNTRY_CODE = "country_code"
const val CURRENCY_CODE = "currency_code"
const val FIRST_NAME = "first_name"
const val LAST_NAME = "last_name"
const val CITY = "city"
//const val EMAIL = "email"
const val PROFILE_PIC_URL = "profile_pic_url"
const val BABY_PROFILE_PIC_URL = "baby_profile_pic_url"

const val SECURE_DEVICE_ID = "secure_device_id"

const val IS_WATER_MARK_PLAN_ACTIVE = "is_water_mark_plan_active" // 1 - Active & 0 - InActive
const val IS_ADS_PLAN_ACTIVE = "is_ads_plan_active" // 1 - Active & 0 - InActive
const val BANNER_KEY = "banner_key"
const val REWARDED_VIDEO_KEY = "rewardedVideoKey"
const val RAZORPAY_KEY = "razorpay_key"
const val IS_BLOCKED = "isBlocked"
const val ROLE = "role"
const val APIKEY = "apikey"

const val JOB_SEEKER = 0
const val RECRUITER = 1


//chat msg types
const val TXT_TYPE = 0
const val PDF_TYPE = 1
const val IMG_TYPE = 2
const val EMG_TYPE = 3

const val JOB_TITLE = "vJobTitle"
const val ADDRESS = "vAddress"
const val WORKING_MODE = "vWrokingMode"
const val SALARY_PACKAGE = "vSalaryPackage"

const val SELECT_PROFILE_IMG = 12
const val SELECT_PROFILE_BANNER_IMG = 24
const val SELECT_RESUME_FILE = 36

const val CURRENT_COMPANY = 1
const val PREV_COMPANY = 0

/*var IS_CHALLENGE_UPLOADED = false
var IS_CHALLENGE_COMMENT_LIKE = false*/

/** Plan Type (iPlanType)
    iPlanType == 0 => Watermark
    iPlanType == 1 => Advertisement
    iPlanType == 2 => Stripe Both Combo pack
 */

/** For Change App Language */
const val LANGUAGE = "language"

const val IS_HOME_FRAGMENT = false
var IS_ADDED_JOB_PREFERENCE = false