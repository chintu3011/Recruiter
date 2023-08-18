package com.amri.emploihunt.networking

import okhttp3.OkHttpClient

object NetworkUtils {

    private const val BASE_URL = "http://192.168.1.2:5000/api"

    const val REGISTER_USER = "$BASE_URL/signin/register_user"
    const val CHECK_USER_EXISTING = "$BASE_URL/signin/check_user_existing"
    const val LOGIN = "$BASE_URL/signin/login"
    const val SIGN_OUT = "$BASE_URL/signin/signout"
    const val GET_ALL_JOB = "$BASE_URL/job/jobs"
    const val INSERT_POST = "$BASE_URL/job/insert_jobs"
    const val GET_USER_BY_ID = "$BASE_URL/user/"
    const val GET_ALL_JOBSEEKER = "$BASE_URL/jobSeeker/get_jobSeeker"
    const val LOGOUT= "$BASE_URL/signin/signout"
    const val GET_CITIES= "$BASE_URL/city/cities"
    const val APPLY= "$BASE_URL/job/apply"
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