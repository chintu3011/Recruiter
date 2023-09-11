package com.amri.emploihunt.networking

import okhttp3.OkHttpClient

object NetworkUtils {

    private const val BASE_URL = "http://192.168.1.5:5000/api"
    /*private const val BASE_URL = "http://192.168.148.196:5000/api"*/
    /*private const val BASE_URL = "http://192.168.223.196:5000/api"*/


    const val REGISTER_USER = "$BASE_URL/signin/register_user"
    const val CHECK_USER_EXISTING = "$BASE_URL/signin/check_user_existing"
    const val LOGIN = "$BASE_URL/signin/login"
    const val SIGN_OUT = "$BASE_URL/signin/signout"
    const val GET_ALL_JOB = "$BASE_URL/job/jobs"
    const val INSERT_POST = "$BASE_URL/job/insert_jobs"
    const val GET_USER_BY_ID = "$BASE_URL/user/"
    const val GET_ALL_JOBSEEKER = "$BASE_URL/jobSeeker/get_jobSeeker"
    const val GET_ALL_RECRUITER = "$BASE_URL/recruiter/get_recruiter"
    const val GET_USER_BY_FIREBASE_ID = "$BASE_URL/user/get_user_by_firebaseId"
    const val LOGOUT= "$BASE_URL/signin/signout"
    const val GET_CITIES= "$BASE_URL/city/cities"
    const val APPLY= "$BASE_URL/job/apply"
    const val APPLY_LIST= "$BASE_URL/job/applyList"
    const val GET_POST_JOB_BY_HR_ID = "$BASE_URL/job/jobs_by_hrId"
    const val GET_POST_JOB_BY_HR_ID_WITHOUT_PAGINATION = "$BASE_URL/job/jobs_by_hrId_withoutpagination"
    const val GET_APPLIED_CANDIDATE_LIST = "$BASE_URL/jobSeeker/get_applied_jobSeeker"

    const val JOB_PREFERENCE= "$BASE_URL/jobPreference/insert_preference"
    const val JOB_PREFERENCE_UPDATE= "$BASE_URL/jobPreference/update_preference"
    const val JOB_PREFERENCE_LIST= "$BASE_URL/jobPreference/jobPreference"
    const val SAVE= "$BASE_URL/job/save"
    const val UN_SAVE= "$BASE_URL/job/unSave"
    const val SAVE_LIST= "$BASE_URL/job/saveList"
    const val UPDATE_POST = "$BASE_URL/job/update_jobs"
    const val DELETE_POST = "$BASE_URL/job/delete"
    const val FIlTER_JOBS = "$BASE_URL/job/jobs_filter"
    const val INSERT_EXPERIENCE = "$BASE_URL/experince/insert_experince"
    const val UPDATE_PROFILE_DETAILS = "$BASE_URL/user/update_profile_details"

    const val GET_LATEST_APP_VERSION_CODE = "$BASE_URL/update_app/get_latest_app_version_code"

    const val CONTACT_US = "$BASE_URL/contact-us/insert_user_contact"
    const val TERMS_PRIVACY = "$BASE_URL/terms-privacy-policy/get_terms_policy_by_id"
    const val GET_ALL_CAMPUS = "$BASE_URL/campus_placement/campus"
    const val CAMPUS_APPLY = "$BASE_URL/campus_placement/apply"


    val okHttpClient: OkHttpClient = OkHttpClient().newBuilder().build()

    fun getOkHttpClientWithHeader(token: String): OkHttpClient {
        return OkHttpClient().newBuilder().authenticator { route, response ->
            response.request.newBuilder().header("Authorization", "Bearer $token").build()
        }.build()
    }

    /** TESTING APIs */
    /* Payment Transaction */
    /*const val INSERT_PAYMENT_TRANSACTION_TEST = "$BASE_URL/payment_transaction/insert_payment_transaction_TEST"
    const val PAYMENT_SECRET_KEY_TEST = "$BASE_URL/payment_transaction/get_stripe_server_key_TEST"*/
}