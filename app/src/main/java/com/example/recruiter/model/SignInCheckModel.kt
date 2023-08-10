package com.example.recruiter.model

data class SignInCheckModel(
    val data: DataSignInCheck,
    val message: String,
    val status: Int
)

data class DataSignInCheck(
    val tAuthToken: String,
    val iUserId: String,
    val vReferralCode: String,
    val tDeviceToken: String,
    val user: User
)